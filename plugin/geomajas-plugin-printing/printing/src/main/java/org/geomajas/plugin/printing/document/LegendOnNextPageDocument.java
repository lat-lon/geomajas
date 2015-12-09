package org.geomajas.plugin.printing.document;

import java.util.List;

import org.geomajas.plugin.printing.component.LegendComponent;
import org.geomajas.plugin.printing.component.PageComponent;
import org.geomajas.plugin.printing.component.PdfContext;
import org.geomajas.plugin.printing.component.PrintComponent;
import org.geomajas.plugin.printing.component.impl.DynamicLegendComponentImpl;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Encapsulates a two page document with the map on the first an legend on the second page.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class LegendOnNextPageDocument extends AbstractItextDocument {

	private LegendComponent<?> legendComponent;

	public LegendOnNextPageDocument(PageComponent page, LegendComponent<?> legendComponent) {
		super(page);
		this.legendComponent = legendComponent;
	}

	protected void renderContent(Document document, PdfWriter writer, PdfContext context) throws DocumentException,
			BadElementException {
		page.layout(context);
		page.render(context);
		document.add(context.getImage());
		if (legendComponent instanceof DynamicLegendComponentImpl) {
			renderLegendOnMultiplePages(document, writer);
		} else {
			document.newPage();
			context = createContext(writer);
			legendComponent.layout(context);
			legendComponent.render(context);
			document.add(context.getImage());
		}
	}

	private void renderLegendOnMultiplePages(Document document, PdfWriter writer) throws DocumentException,
			BadElementException {
		DynamicLegendComponentImpl dynamicLegendComponent = (DynamicLegendComponentImpl) legendComponent;
		legendComponent.setBounds(page.getBounds());
		PdfContext context = createContext(writer);
		List<List<PrintComponent<?>>> pages = dynamicLegendComponent.layoutOnMultiplePages(createContext(writer));
		for (List<PrintComponent<?>> page : pages) {
			if(!page.isEmpty()){
				document.newPage();
				context = createContext(writer);
				page.add(dynamicLegendComponent.getTitleLabel());
				render(context, page);
				Image image = context.getImage();
				document.add(image);
			}
		}
	}

	private void render(PdfContext context, List<PrintComponent<?>> page) {
		for (PrintComponent<?> child : page) {
			context.saveOrigin();
			context.setOrigin(child.getBounds().getLeft(), child.getBounds().getBottom());
			child.render(context);
			context.restoreOrigin();
		}
	}

}