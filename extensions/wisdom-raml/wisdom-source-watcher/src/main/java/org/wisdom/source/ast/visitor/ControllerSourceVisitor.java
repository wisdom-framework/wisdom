/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2015 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.source.ast.visitor;

import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.source.ast.model.ControllerModel;
import org.wisdom.source.ast.model.ControllerRouteModel;
import org.wisdom.source.ast.model.RouteParamModel;
import org.wisdom.source.ast.util.NameConstant;

import java.util.List;
import java.util.Set;

import static org.wisdom.source.ast.model.RouteParamModel.ParamType;
import static org.wisdom.source.ast.model.RouteParamModel.ParamType.*;
import static org.wisdom.source.ast.util.ExtractUtil.*;

/**
 * Visit the controller file AST in order to populate a {@link ControllerRouteModel}.
 *
 * @author barjo
 */
public final class ControllerSourceVisitor extends VoidVisitorAdapter<ControllerModel> implements NameConstant {

    /**
     * Use an instance the maven plugin SystemStreamLog as logger.
     */
    private static final Log LOGGER = new SystemStreamLog();

    /**
     * Visit the route methods and construct ControllerRouteModel.
     */
    private static final RouteMethodSourceVisitor routeVisitor = new RouteMethodSourceVisitor();

    /**
     * Visit the route parameters and construct the RouteParamModel.
     */
    private static final RouteParamSourceVisitor paramVisitor = new RouteParamSourceVisitor();

    /**
     * Visit the class declaration, this is the visitor entry point!
     *
     * @param declaration {@inheritDoc}
     * @param controller The ControllerModel we are building.
     */
    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, ControllerModel controller) {
        controller.setName(declaration.getName());

        LOGGER.info("[controller]Visit " + controller.getName());
        //Go on with the methods and annotations
        super.visit(declaration,controller);
    }

    /**
     * Visit the controller normal annotations.
     * <p>
     *  We add the value of the Path annotation as the ControllerModel base Path.
     * </p>
     * @param anno {@inheritDoc}
     * @param controller The ControllerModel we are building.
     */
    @Override
    public void visit(NormalAnnotationExpr anno, ControllerModel controller) {

        //org.wisdom.api.annotations.Path (There is only one pair ->value="")
        if(anno.getName().getName().equals(ANNOTATION_PATH)){
            java.lang.String path = asString(anno.getPairs().get(0).getValue());
            controller.setBasePath(path);
        }
    }

    /**
     * We ignore the InitializerDeclaration.
     *
     * @param n {@inheritDoc}
     * @param arg {@inheritDoc}
     */
    @Override
    public void visit(InitializerDeclaration n, ControllerModel arg) {
        //Ignore initializer declaration, related to issue #4
    }

    /**
     * We ignore the FieldDeclaration.
     *
     * @param n {@inheritDoc}
     * @param arg {@inheritDoc}
     */
    @Override
    public void visit(FieldDeclaration n, ControllerModel arg) {
        //ignore field
    }

    /**
     * We ignore the FieldAccessExpr.
     *
     * @param n {@inheritDoc}
     * @param arg {@inheritDoc}
     */
    @Override
    public void visit(FieldAccessExpr n, ControllerModel arg) {
        //ignore field
    }

    /**
     * Similar to {@link #visit(NormalAnnotationExpr, ControllerModel)}.
     *
     * @param anno {@inheritDoc}
     * @param controller The ControllerModel we are building.
     */
    @Override
    public void visit(SingleMemberAnnotationExpr anno, ControllerModel controller) {

        //org.wisdom.api.annotations.Path
        if(anno.getName().getName().equals(ANNOTATION_PATH)){
            controller.setBasePath(asString(anno.getMemberValue()));
        }
    }

    /**
     * Visit the Controller JavaDoc block.
     * <p>
     * Add the JavadocComment as the ControllerModel description.
     * Set the ControllerModel version as the javadoc version tag if it exists.
     * </p>
     *
     * @param jdoc {@inheritDoc}
     * @param controller The ControllerModel we are building.
     */
    @Override
    public void visit(JavadocComment jdoc, ControllerModel controller) {
        controller.setDescription(extractDescription(jdoc));

        Set<String> version = extractDocAnnotation("@version",jdoc);
        if(!version.isEmpty()){
            controller.setVersion(version.iterator().next());
        }
    }

    /**
     * Visit the Controller methods.
     * <p>
     * We visit each methods that are annotated with the Route annotations with the {@link RouteMethodSourceVisitor}.
     * The routes are add to the model indexed by their Path. The routes are ordered according to natural ordering
     * and their hierarchy.
     * </p>
     *
     * @param method {@inheritDoc}
     * @param controller The ControllerModel we are building.
     */
    @Override
    public void visit(MethodDeclaration method, ControllerModel controller) {

        List<AnnotationExpr> annos = method.getAnnotations();

        if(annos ==null || annos.isEmpty()){
            return;
        }

        for(AnnotationExpr anno: annos){
            if(anno.getName().getName().equals(ANNOTATION_ROUTE)){
                ControllerRouteModel route = new ControllerRouteModel();
                route.setMethodName(method.getName());

                routeVisitor.visit(method, route); //controllerParsed the method, annotations and params

                LOGGER.info("[controller]The route method " + route.getMethodName() + " starting at line " +
                        method.getBeginLine() + " has been properly visited.");

                //add the route to the controller
                controller.addRoute(route);
            }
        }
    }

    /**
     * Visit the methods, and their annotations
     */
    private static final class RouteMethodSourceVisitor extends VoidVisitorAdapter<ControllerRouteModel>{

        /**
         * Visit the Annotations of a Route method.
         *
         * @param anno Normal annotation on the route method.
         * @param route The route model that we construct.
         */
        @Override
        public void visit(NormalAnnotationExpr anno, ControllerRouteModel route) {

            //Ignore methods that are not annotated with the Route annotation.
            if(!anno.getName().getName().equals(ANNOTATION_ROUTE)) {
                return;
            }

            for (MemberValuePair pair : anno.getPairs()){

                switch (pair.getName()) {
                    case "method":
                        //TODO Do some check here ?
                        route.setHttpMethod(HttpMethod.valueOf(pair.getValue().toString().replace("HttpMethod.", "")));
                        break;
                    case "uri":
                        route.setPath(asString(pair.getValue()));
                        break;
                    case ROUTE_ACCEPTS:
                        route.setBodyMimes(asStringSet(pair.getValue()));
                        break;
                    case ROUTE_PRODUCES:
                        route.setResponseMimes(asStringSet(pair.getValue()));
                        break;
                    default:
                        break; //unknown route attributes
                }
            }
        }

        /**
         * Visit the parameter of a Route method.
         * @param param Parameter of the route method.
         * @param route The route model that we construct.
         */
        @Override
        public void visit(Parameter param, ControllerRouteModel route) {
            List<AnnotationExpr> annos = param.getAnnotations();

            if(annos == null || annos.isEmpty()){
                LOGGER.warn("[controller]The parameter " + param + "at line " + param.getBeginLine() + " " +
                        "is for a route method but has not been annotated!");
                return;
            }

            RouteParamModel routeParam = new RouteParamModel();

            routeParam.setParamName(String.valueOf(param.getId()));
            routeParam.setName(routeParam.getParamName()); //by default, will be override if name is specified

            //Parsed the param (for the annotation)
            paramVisitor.visit(param,routeParam);

            if(routeParam.getParamType() == null){ //ignore if the param has not been visited (i.e no annotations)
                return;
            }

            //TODO some cleaning here!
            routeParam.setValueType(param.getType().toString());

            route.addParam(routeParam);
        }

        /**
         * Add the comment content.
         *
         * @param comment BlockComment on the route method.
         * @param route The route model that we construct.
         */
        @Override
        public void visit(BlockComment comment, ControllerRouteModel route) {
            route.setDescription(comment.getContent());
        }

        /**
         * Add the javadoc content.
         * @param comment JavadocComment on the route method.
         * @param route The route model that we construct.
         */
        @Override
        public void visit(JavadocComment comment, ControllerRouteModel route) {
            //extract the body sample annotation if present
            route.setBodySamples(extractBodySample(comment));

            //extract the description before the jdoc annotation
            route.setDescription(extractDescription(comment));
        }
    }

    /**
     * Visit the Route params and their annotation.
     */
    private static final class RouteParamSourceVisitor extends VoidVisitorAdapter<RouteParamModel>{

        @Override
        public void visit(NormalAnnotationExpr anno, RouteParamModel param) {

            if(anno.getName().getName().equals(CONSTRAINT_NOTNULL)){
                param.setMandatory(true);
                return;
            }

            if(anno.getName().getName().equals(CONSTRAINT_MIN)){
                param.setMin(Long.valueOf(extractValueByName(anno.getPairs(),"value")));
                return;
            }

            if(anno.getName().getName().equals(CONSTRAINT_MAX)){
                param.setMax(Long.valueOf(extractValueByName(anno.getPairs(), "value")));
                return;
            }

            if(anno.getName().getName().equals(ANNOTATION_PARAM)){
                param.setParamType(PARAM);

            } else if(anno.getName().getName().equals(ANNOTATION_PATH_PARAM)){
                param.setParamType(PATH_PARAM);

            } else if(anno.getName().getName().equals(ANNOTATION_QUERYPARAM)){
                param.setParamType(QUERY);

            } else if(anno.getName().getName().equals(ANNOTATION_FORMPARAM)){
                param.setParamType(FORM);

            } else if(anno.getName().getName().equals(ANNOTATION_DEFAULTVALUE)){
                param.setDefaultValue(asString(anno.getPairs().get(0).getValue()));

            } else{
                LOGGER.warn("[controller]Annotation " + anno + " at line " + anno.getBeginLine() + " " +
                        "is unknown!");
                return;
            }

            //Only one member with name!
            param.setName(asString(anno.getPairs().get(0).getValue()));
        }

        @Override
        public void visit(SingleMemberAnnotationExpr anno, RouteParamModel param) {

            if(anno.getName().getName().equals(ANNOTATION_DEFAULTVALUE)){
                param.setDefaultValue(asString(anno.getMemberValue()));
                return;
            }

            if(anno.getName().getName().equals(CONSTRAINT_MIN)){
                param.setMin(Long.valueOf(anno.getMemberValue().toString()));
                return;
            }

            if(anno.getName().getName().equals(CONSTRAINT_MAX)){
                param.setMax(Long.valueOf(anno.getMemberValue().toString()));
                return;
            }

            if(anno.getName().getName().equals(ANNOTATION_PARAM)){
                param.setParamType(PARAM);

            } else if(anno.getName().getName().equals(ANNOTATION_PATH_PARAM)){
                param.setParamType(PATH_PARAM);

            } else if(anno.getName().getName().equals(ANNOTATION_QUERYPARAM)){
                param.setParamType(QUERY);

            } else if(anno.getName().getName().equals(ANNOTATION_FORMPARAM)){
                param.setParamType(FORM);

            } else{
                LOGGER.warn("[controller]Annotation " + anno + " at line " + anno.getBeginLine() + " " +
                        "is unknown!");
                return;
            }

            param.setName(asString(anno.getMemberValue()));
        }

        @Override
        public void visit(MarkerAnnotationExpr anno, RouteParamModel param) {

            if (anno.getName().getName().equals(ANNOTATION_BODY)) {
                param.setParamType(ParamType.BODY);

            } else if (anno.getName().getName().equals(CONSTRAINT_NOTNULL)){
                param.setMandatory(true);

            } else {
                LOGGER.warn("[controller]Annotation " + anno + " at line " + anno.getBeginLine() + " " +
                        "is unknown!");
            }
        }
    }
}
