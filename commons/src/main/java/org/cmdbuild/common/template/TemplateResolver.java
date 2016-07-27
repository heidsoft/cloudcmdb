package org.cmdbuild.common.template;

/**
 * Resolver for templates.
 */
public interface TemplateResolver {

	/**
	 * Resolves the specified template. A {@code null} template should be
	 * resolved as {@code null}.
	 * 
	 * @param expression
	 * 
	 * @return the resolved template.
	 */
	String resolve(String expression);

}
