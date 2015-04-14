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
import org.wisdom.api.http.HttpMethod;
import org.wisdom.source.ast.model.ControllerModel;
import org.wisdom.source.ast.model.ControllerRouteModel;
import org.wisdom.source.ast.model.RouteParamModel;
import org.wisdom.source.ast.util.NameConstant;

import java.util.List;

import static org.wisdom.source.ast.model.RouteParamModel.ParamType;
import static org.wisdom.source.ast.model.RouteParamModel.ParamType.*;
import static org.wisdom.source.ast.util.ExtractUtil.*;

/**
 * Visit the controller file AST in order to populate a {@link ControllerRouteModel}.
 *
 * @author barjo
 */
public class ControllerSourceVisitor extends VoidVisitorAdapter<ControllerModel> implements NameConstant {

    private final Log logger;

    /**
     * To controllerParsed the route methods.
     */
    private RouteMethodSourceVisitor routeVisitor = new RouteMethodSourceVisitor();

    /**
     * To controllerParsed the route parameters.
     */
    private RouteParamSourceVisitor paramVisitor = new RouteParamSourceVisitor();


    public ControllerSourceVisitor(Log logger) {
        this.logger = logger;
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, ControllerModel controller) {
        controller.setName(declaration.getName());

        logger.info("[controller]Visit "+controller.getName());
        //Go on with the methods and annotations
        super.visit(declaration,controller);
    }

    @Override
    public void visit(NormalAnnotationExpr anno, ControllerModel controller) {

        //org.wisdom.api.annotations.Path (There is only one pair ->value="")
        if(anno.getName().getName().equals(ANNOTATION_PATH)){
            java.lang.String path = asString(anno.getPairs().get(0).getValue());
            controller.setBasePath(path);
        }
    }

    @Override
    public void visit(InitializerDeclaration n, ControllerModel arg) {
        //Ignore initializer declaration, related to issue #4
    }

    @Override
    public void visit(FieldDeclaration n, ControllerModel arg) {
        //ignore field
    }

    @Override
    public void visit(FieldAccessExpr n, ControllerModel arg) {
        //ignore field
    }

    @Override
    public void visit(SingleMemberAnnotationExpr anno, ControllerModel controller) {

        //org.wisdom.api.annotations.Path
        if(anno.getName().getName().equals(ANNOTATION_PATH)){
            controller.setBasePath(asString(anno.getMemberValue()));
        }
    }

    @Override
    public void visit(JavadocComment jdoc, ControllerModel controller) {
        controller.setDescription(extractDescription(jdoc));

        List<String> version = extractDocAnnotation("@version",jdoc);
        if(!version.isEmpty()){
            controller.setVersion(version.get(0));
        }
    }

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

                logger.info("[controller]The route method " + route.getMethodName() + " starting at line " +
                        method.getBeginLine() + " has been properly visited.");

                //add the route to the controller
                controller.addRoute(route);
            }
        }
    }

    /**
     * Visit the methods, and their annotations
     */
    private class RouteMethodSourceVisitor extends VoidVisitorAdapter<ControllerRouteModel>{

        /**
         * Visit the Annotations of a Route method.
         *
         * @param anno Normal annotation on the route method.
         * @param route The route model that we construct.
         */
        @Override
        public void visit(NormalAnnotationExpr anno, ControllerRouteModel route) {

            //org.wisdom.api.annotations.Route
            if(anno.getName().getName().equals(ANNOTATION_ROUTE)){

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
                            route.setBodyMimes(asStringList(pair.getValue()));
                            break;
                        case ROUTE_PRODUCES:
                            route.setResponseMimes(asStringList(pair.getValue()));
                            break;
                    }
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
                logger.warn("[controller]The parameter " + param + "at line " + param.getBeginLine() + " " +
                        "is for a route method but has not been annotated!");
                return;
            }

            RouteParamModel routeParam = new RouteParamModel();

            routeParam.setParamName(String.valueOf(param.getId()));
            routeParam.setName(routeParam.getParamName()); //by default, will be override if name is specified

            //controllerParsed the param (for the annotation)
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
     * controllerParsed the Route params and their annotation
     */
    private class RouteParamSourceVisitor extends VoidVisitorAdapter<RouteParamModel>{

        @Override
        public void visit(NormalAnnotationExpr anno, RouteParamModel param) {

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
                logger.warn("[controller]Annotation " + anno + " at line " + anno.getBeginLine() + " " +
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

            } if(anno.getName().getName().equals(ANNOTATION_PARAM)){
                param.setParamType(PARAM);

            } else if(anno.getName().getName().equals(ANNOTATION_PATH_PARAM)){
                param.setParamType(PATH_PARAM);

            } else if(anno.getName().getName().equals(ANNOTATION_QUERYPARAM)){
                param.setParamType(QUERY);

            } else if(anno.getName().getName().equals(ANNOTATION_FORMPARAM)){
                param.setParamType(FORM);

            } else{
                logger.warn("[controller]Annotation " + anno + " at line " + anno.getBeginLine() + " " +
                        "is unknown!");
                return;
            }

            param.setName(asString(anno.getMemberValue()));
        }


        @Override
        public void visit(MarkerAnnotationExpr anno, RouteParamModel param) {
            if (anno.getName().getName().equals(ANNOTATION_BODY)) {
                param.setParamType(ParamType.BODY);
            } else {
                logger.warn("[controller]Annotation " + anno + " at line " + anno.getBeginLine() + " " +
                        "is unknown!");
            }
        }
    }
}
