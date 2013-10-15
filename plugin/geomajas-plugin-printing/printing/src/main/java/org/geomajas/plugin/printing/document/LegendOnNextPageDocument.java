package org.geomajas.plugin.printing.document;

import org.geomajas.plugin.printing.component.LegendComponent;
import org.geomajas.plugin.printing.component.PageComponent;
import org.geomajas.plugin.printing.component.PdfContext;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Encapulates a two page document with the map on the first an legend on the second page.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class LegendOnNextPageDocument extends AbstractItextDocument {

	private LegendComponent legendComponent;

	public LegendOnNextPageDocument(PageComponent page, LegendComponent legendComponent) {
		super(page);
		this.legendComponent = legendComponent;
	}

	protected void renderContent(Document document, PdfWriter writer, PdfContext context) throws DocumentException,
			BadElementException {
		page.layout(context);
		// finally render
		page.render(context);
		document.add(context.getImage());

		document.newPage();
		// writer = createWriter(document);
		context = createContext(writer);
		legendComponent.layout(context);
		legendComponent.render(context);
		document.add(context.getImage());
	}

}