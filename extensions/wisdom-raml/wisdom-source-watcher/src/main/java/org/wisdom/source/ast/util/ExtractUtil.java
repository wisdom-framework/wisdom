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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

/**
 * A set of function that helps to extract various entity from the javaparser AST.
 *
 * @author barjo
 */
public class ExtractUtil implements NameConstant {

    private static final class StringExtractor extends GenericVisitorAdapter<String, String> {
		@Override
		public String visit(StringLiteralExpr n, String arg) {
		    String string = n.toString();

		    if("\"\"".equals(string)){
		        return "";
		    }

		    return string.substring(1,string.length()-1);
		}

		@Override
		public String visit(FieldAccessExpr n, String arg) {
			return visit(n.getFieldExpr(), findClassNamed(n.getScope(), getClassDeclarationOf(n)));
		}

		private ClassOrInterfaceDeclaration findClassNamed(Expression scope, ClassOrInterfaceDeclaration classDeclarationOf) {
			for(BodyDeclaration b : classDeclarationOf.getMembers()) {
				if (b instanceof ClassOrInterfaceDeclaration) {
					ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) b;
					if(classDeclaration.getName().equals(scope.toString())) {
						return classDeclaration;
					}
				}
			}
			// Not found ? Damn, maybe it is declared in a damned local class
			if(classDeclarationOf.getParentNode() instanceof ClassOrInterfaceDeclaration) {
				return findClassNamed(scope, (ClassOrInterfaceDeclaration) classDeclarationOf.getParentNode());
			} else {
				Node node = classDeclarationOf;
				while(node.getParentNode()!=null) {
					node  = node.getParentNode();
				}
				if (node instanceof CompilationUnit) {
					CompilationUnit cu = (CompilationUnit) node;
					for(TypeDeclaration d : cu.getTypes()) {
						if (d instanceof ClassOrInterfaceDeclaration) {
							ClassOrInterfaceDeclaration classDeclaration = (ClassOrInterfaceDeclaration) d;
							if(classDeclaration.getName().equals(scope.toString())) {
								return classDeclaration;
							}
						}
					}
				}
			}
			throw new UnsupportedOperationException(
					String.format("Can't find declaration of %s in %s. String extraction from another file doesn't work. Sorry.",
							scope.toString(), classDeclarationOf.getName()));
		}

		@Override
		public String visit(NameExpr n, String arg) {
			return visit(n, getClassDeclarationOf(n));
		}

		private String visit(NameExpr n, ClassOrInterfaceDeclaration classDeclarationOf) {
			return evaluateFieldNamed(n.getName(), classDeclarationOf);
		}

		private String evaluateFieldNamed(String name, ClassOrInterfaceDeclaration classDeclarationOf) {
			for(BodyDeclaration b : classDeclarationOf.getMembers()) {
				if (b instanceof FieldDeclaration) {
					FieldDeclaration fieldDeclaration = (FieldDeclaration) b;
					for(VariableDeclarator variable : fieldDeclaration.getVariables()) {
						if(variable.getId().getName().equals(name)) {
							return variable.getInit().accept(this, "");
						}
					}
				}
			}
			return "";
		}

		private ClassOrInterfaceDeclaration getClassDeclarationOf(Node n) {
			while(!(n instanceof ClassOrInterfaceDeclaration)) {
				n = n.getParentNode();
			}
			return (ClassOrInterfaceDeclaration) n;
		}
	}

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
    	return node.accept(new StringExtractor(), "");
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
