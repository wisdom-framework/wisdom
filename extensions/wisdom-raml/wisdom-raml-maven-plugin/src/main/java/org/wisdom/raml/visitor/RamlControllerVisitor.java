/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2015 Wisdom Framework
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

package org.wisdom.raml.visitor;

import org.raml.model.*;
import org.raml.model.parameter.AbstractParam;
import org.raml.model.parameter.FormParameter;
import org.raml.model.parameter.QueryParameter;
import org.raml.model.parameter.UriParameter;
import org.wisdom.source.ast.model.ControllerModel;
import org.wisdom.source.ast.model.ControllerRouteModel;
import org.wisdom.source.ast.model.RouteParamModel;
import org.wisdom.source.ast.visitor.Visitor;

import java.util.*;

import static java.util.Collections.singletonList;

/**
 * <p>
 * {@link ControllerModel} visitor implementation. It populates a given {@link Raml} model thanks to the
 * wisdom {@link ControllerModel}.
 * </p>
 *
 * @author barjo
 */
public class RamlControllerVisitor implements Visitor<ControllerModel<Raml>,Raml> {

    /**
     * Visit the Wisdom Controller source model in order to populate the raml model.
     *
     * @param element The wisdom controller model (we visit it).
     * @param raml The raml model (we construct it).
     */
    @Override
    public void visit(ControllerModel element,Raml raml) {
        raml.setTitle(element.getName());

        if(element.getDescription() != null && !element.getDescription().isEmpty()){
            DocumentationItem doc = new DocumentationItem();
            doc.setContent(element.getDescription());
            doc.setTitle("Description");
            raml.setDocumentation(singletonList(doc));
        }

        if(element.getVersion()!=null && !element.getVersion().isEmpty()){
            raml.setVersion(element.getVersion());
        }

        NavigableMap<String,Collection<ControllerRouteModel>> routes = element.getRoutes().asMap();
        navigateTheRoutes(routes, null,raml);
    }

    /**
     * Navigate through the Controller routes, and create {@link org.raml.model.Resource} from them.
     * If the <code>parent</code> is not null, then the created route will be added has children of the parent, otherwise
     * a new Resource is created and will be added directly to the <code>raml</code> model.
     *
     * @param routes The @{link ControllerRoute}
     * @param parent The parent {@link Resource}
     * @param raml The {@link Raml} model
     */
    private void navigateTheRoutes(NavigableMap<String, Collection<ControllerRouteModel>> routes, Resource parent,Raml raml){
        //nothing to see here
        if (routes == null || routes.isEmpty()){
            return; //
        }

        String headUri = routes.firstKey();

        Collection<ControllerRouteModel> brotherElems = routes.get(headUri);

        Resource res = new Resource();

        if(parent!=null) {
            res.setParentResource(parent);
            res.setParentUri(parent.getUri());
            //Get the relative part of the url, and remove last character `/` (added for ordering)
            res.setRelativeUri(headUri.substring(parent.getUri().length(),headUri.length()-1));
            parent.getResources().put(res.getRelativeUri(), res);
        }else {
            res.setParentUri("");
            res.setRelativeUri(headUri.substring(0,headUri.length()-1)); //remove last character `/`
            //update raml
            raml.getResources().put(res.getRelativeUri(),res);
        }

        //Add the action from the brother routes
        for(ControllerRouteModel bro : brotherElems){
            addActionFromRouteElem(bro,res);
        }

        //visit the children route
        NavigableMap<String,Collection<ControllerRouteModel>> child = routes.tailMap(headUri, false);

        //no more route element
        if(child.isEmpty()){
            return;
        }

        if(child.firstKey().startsWith(headUri)){
            navigateTheRoutes(child, res, raml); //current resource is the parent
        }else{
            navigateTheRoutes(child, parent, raml); //same parent as this resource
        }
    }

    /**
     *
     * @param elem
     * @param resource
     */
    private void addActionFromRouteElem(ControllerRouteModel<Raml> elem, Resource resource){
        Action action = new Action();
        action.setType(ActionType.valueOf(elem.getHttpMethod().name()));

        action.setDescription(elem.getDescription());

        //handle body
        action.setBody(new LinkedHashMap<String, MimeType>(elem.getBodyMimes().size()));
        for(String mime: elem.getBodyMimes()){
            action.getBody().put(mime,new MimeType(mime)); //TODO enhance with sample
        }

        for(String mime: elem.getResponseMimes()){
            Response resp = new Response();
            resp.setBody(Collections.singletonMap(mime, new MimeType(mime)));
            action.getResponses().put("200",resp); //TODO enhance with sample
        }

        //handle all route params
        for(RouteParamModel<Raml> param: elem.getParams()){

            AbstractParam ap = null; //the param to add

            //Fill the param info depending on its type
            switch (param.getParamType()){
                case BODY:
                    continue; //skip - body is handled at the method level
                case FORM:
                    MimeType formMime = action.getBody().get("application/x-www-form-urlencoded");

                    if(formMime == null){
                        //create default form mimeType
                        formMime = new MimeType("application/x-www-form-urlencoded");
                    }

                    if(formMime.getFormParameters() == null){ //why raml, why you ain't init that
                        formMime.setFormParameters(new LinkedHashMap<String, List<FormParameter>>(2));
                    }

                    ap = new FormParameter();
                    formMime.getFormParameters().put(param.getName(), singletonList((FormParameter) ap));
                    break;
                case PARAM:
                case PATH_PARAM:
                    if (ancestorOrIHasParam(resource, param.getName())) {
                        continue; //skip, the parent already has it defined
                    }

                    ap = new UriParameter();
                    resource.getUriParameters().put(param.getName(), (UriParameter) ap);
                    break;
                case QUERY:
                     ap = new QueryParameter();
                     action.getQueryParameters().put(param.getName(),(QueryParameter) ap);
                    break;
            }

            //ap.setDisplayName(param.getName());

            //Set param type
            ParamType type = typeConverter(param.getValueType());

            if(type != null) {
                ap.setType(type);
            }

            //set default value
            ap.setRequired(true); //required by default ? TODO use the optional annotations.

            if(param.getDefaultValue()!=null){
                ap.setRequired(false);
                ap.setDefaultValue(param.getDefaultValue());
            }
        }

        resource.getActions().put(action.getType(),action);
    }

    private static Boolean ancestorOrIHasParam(final Resource resource, String uriParamName){
        Resource ancestor = resource;

        while(ancestor!=null){
            if(ancestor.getUriParameters().containsKey(uriParamName)){
                return true;
            }
            ancestor = ancestor.getParentResource();
        }

        return false;
    }

    /**
     * @param typeName The type name
     * @return the {@link ParamType} corresponding to the  given type name.
     */
    private static ParamType typeConverter(String typeName){
        if(typeName == null || typeName.isEmpty()){
            return null;
        }

        if(typeName.equals("Number") || typeName.equalsIgnoreCase("Long") || typeName.equals("Integer") || typeName.equals("int")){
            return ParamType.NUMBER;
        }

        if(typeName.equalsIgnoreCase("Boolean") ){
            return ParamType.BOOLEAN;
        }

        if(typeName.equals("String")){
            return ParamType.STRING;
        }

        if(typeName.equals("Date")){
            return ParamType.DATE;
        }

        if(typeName.equals("File")){
            return ParamType.FILE;
        }

        return null;
    }
}
