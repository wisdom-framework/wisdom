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
package org.wisdom.source.ast.util;

import static java.util.Collections.singleton;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

/**
 * A set of function that helps to extract various entity from the javaparser AST.
 *
 * @author barjo
 */
public class ExtractUtil implements NameConstant {

    /**
     * Hide implicit public constructor.
     */
    private ExtractUtil(){
    }

    /**
     * Extract the String value of the <code>node</code>.
     * It removes the {@literal "}
     *
     * @param node The java node which value to convert into string
     * @return string version of the node value.
     */
    public static String asString(Node node){
//    	return node.accept(new GenericVisitorAdapter<String, String>() {
//		}, "");
        String string = node.toString();

        if("\"\"".equals(string)){
            return "";
        }

        return string.substring(1,string.length()-1);
    }

    /**
     * <p>
     * Extract the String value for each child of the <code>node</code> or the node itself if it does not
     * have children.
     * </p>
     *
     * <p>It removes the {@literal "}.</p>
     * @param node The java node which children or value to convert into a list of string.
     * @return list of the node children string value or singleton list with the value of the node if no children.
     */
    public static Set<String> asStringSet(Node node){

        if(node.getChildrenNodes() == null || node.getChildrenNodes().isEmpty()){
            return singleton(asString(node));
        }

        Set<String> list = new LinkedHashSet<>(node.getChildrenNodes().size());

        for(Node child: node.getChildrenNodes()){
            list.add(asString(child));
        }

        return list;
    }

    /**
     * Return the body samples as String from a JavaDoc comment block.
     *
     * @param jdoc The javadoc block comment.
     * @return the body samples as String
     */
    public static Set<String> extractBodySample(JavadocComment jdoc){
        return extractDocAnnotation(DOC_BODY_SAMPLE,jdoc);
    }

    /**
     * Get a text description from JavaDoc block comment.
     *
     * @param jdoc the javadoc block comment.
     * @return The description as String.
     */
    public static String extractDescription(JavadocComment jdoc){
        String content = jdoc.getContent().replaceAll("\n[ \t]+\\* ?","\n"); //remove the * at the beginning of a line
        int end = content.indexOf("\n@"); //look for the first annotation

        //The first charater is always a new line

        if(end>0) {
            return content.substring(1, end).trim();
        }

        return content.substring(1).trim();
    }

    /**
     * Get the value of a {@link MemberValuePair} present in a list from its name.
     *
     * @param pairs The list of MemberValuePair
     * @param name The name of the MemberValuePair we want to get the value from.
     * @return The value of the MemberValuePair of given name, or null if it's not present in the list.
     */
    public static String extractValueByName(List<MemberValuePair> pairs, String name){
        for(MemberValuePair pair : pairs){
            if(pair.getName().equals(name)){
                return pair.getValue().toString();
            }
        }
        return null;
    }

    /**
     * <p>
     * Extract the content of a doc annotation. The content is always handle as a string.
     * The javadoc * and all tabs/space before it are removed as well as the first space after it if present.
     * </p>
     *
     * <p>
     * The end of the annotation content is either the end of the content block or the start of an other annotation.
     * (i.e it reach a <code>\n@</code> string.
     * </p>
     *
     * @param anno The annotation we are looking for. (must start with @)
     * @param jdoc The javadoc block from were the content will be extracted.
     * @return the content of the annotation, one entry in the list for each annotation encountered.
     */
    public static Set<String> extractDocAnnotation(String anno, JavadocComment jdoc){
        String content = jdoc.getContent().replaceAll("\n[ \t]+\\* ?","\n"); //remove the * at the beginning of a line
        Set<String> result = new LinkedHashSet<>();

        while(content.contains(anno)){
            int begin = content.indexOf(anno)+ (anno).length();
            int end = content.indexOf("\n@",begin); //next annotation

            if(end > 0){
                result.add(content.substring(begin,end).trim());
                content = content.substring(end);
            }else {
                result.add(content.substring(begin).trim());
                content = ""; //no more
            }
        }

        return result;
    }
}
