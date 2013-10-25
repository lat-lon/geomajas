/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.plugin.printing.document;

import java.util.HashMap;
import java.util.Map;

import org.geomajas.plugin.printing.component.MapComponent;
import org.geomajas.plugin.printing.component.PageComponent;
import org.geomajas.plugin.printing.component.PdfContext;
import org.geomajas.plugin.printing.component.PrintComponent;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Single page document for printing.
 * 
 * @author Jan De Moerloose
 */
public class SinglePageDocument extends AbstractItextDocument {

	/** Filters to apply to layers. */
	protected Map<String, String> filters;

	/**
	 * Constructs a document with the specified dimensions.
	 * 
	 * @param page
	 *            page
	 * @param filters
	 *            filters
	 */
	public SinglePageDocument(PageComponent page, Map<String, String> filters) {
		super(page);
		this.filters = (filters == null ? new HashMap<String, String>() : filters);

		// set filters
		for (PrintComponent<?> comp : getPage().getChildren()) {
			if (comp instanceof MapComponent) {
				((MapComponent) comp).setFilter(filters);
			}
		}
	}

	public PageComponent getPage() {
		return page;
	}

	@Override
	protected void renderContent(Document document, PdfWriter writer, PdfContext context) throws DocumentException,
			BadElementException {
		// second pass to layout
		page.layout(context);
		// finally render
		page.render(context);
		document.add(context.getImage());
	}

}