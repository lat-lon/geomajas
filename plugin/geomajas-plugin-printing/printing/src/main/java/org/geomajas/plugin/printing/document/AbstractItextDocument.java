package org.geomajas.plugin.printing.document;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.geomajas.plugin.printing.PrintingException;
import org.geomajas.plugin.printing.component.PageComponent;
import org.geomajas.plugin.printing.component.PdfContext;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

/**
 * 
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class AbstractItextDocument extends AbstractDocument {

	/** The page to render. */
	protected PageComponent page;

	/** In-memory output stream to know content length. */
	protected ByteArrayOutputStream baos;

	/**
	 * Constructs a document with the specified dimensions.
	 * 
	 * @param page
	 *            page
	 */
	public AbstractItextDocument(PageComponent page) {
		this.page = page;
	}

	@Override
	public void render(OutputStream outputStream, Format format) throws PrintingException {
		try {
			doRender(outputStream, format);
		} catch (Exception e) { // NOSONAR
			throw new PrintingException(e, PrintingException.DOCUMENT_RENDER_PROBLEM);
		}
	}

	@Override
	public int getContentLength() {
		return baos == null ? 0 : baos.size();
	}

	/**
	 * Re-calculates the layout and renders to internal memory stream. Always call this method before calling render()
	 * to make sure that the latest document changes have been taken into account for rendering.
	 * 
	 * @param format
	 *            format
	 * @throws PrintingException
	 *             oops
	 */
	public void layout(Format format) throws PrintingException {
		try {
			doRender(null, format);
		} catch (Exception e) { // NOSONAR
			throw new PrintingException(e, PrintingException.DOCUMENT_LAYOUT_PROBLEM);
		}
	}

	protected void doRender(OutputStream outputStream, Format format) throws IOException, DocumentException,
			PrintingException {
		// first render or re-render for different layout
		if (outputStream == null || baos == null || null != format) {
			if (baos == null) {
				baos = new ByteArrayOutputStream(10 * 1024);
			}
			baos.reset();
			boolean resize = false;
			if (page.getBounds().getWidth() == 0 || page.getBounds().getHeight() == 0) {
				resize = true;
			}
			// Create a document in the requested ISO scale.
			Document document = new Document(page.getBounds(), 0, 0, 0, 0);
			PdfWriter writer = createWriter(document);

			// The mapView is not scaled to the document, we assume the mapView
			// has the right ratio.

			// Write document title and metadata
			document.open();
			PdfContext context = createContext(writer);
			// first pass of all children to calculate size
			page.calculateSize(context);
			if (resize) {
				// we now know the bounds of the document
				// round 'm up and restart with a new document
				int width = (int) Math.ceil(page.getBounds().getWidth());
				int height = (int) Math.ceil(page.getBounds().getHeight());
				page.getConstraint().setWidth(width);
				page.getConstraint().setHeight(height);
				document = new Document(new Rectangle(width, height), 0, 0, 0, 0);
				writer = createWriter(document);
				document.open();

				baos.reset();
				context = createContext(writer);
			}
			// int compressionLevel = writer.getCompressionLevel(); // For testing
			// writer.setCompressionLevel(0);

			// Actual drawing
			document.addTitle("Geomajas");

			renderContent(document, writer, context);

			document.close();
			convertToNonPdfFormat(format);
			if (outputStream != null) {
				baos.writeTo(outputStream);
			}
		} else {
			baos.writeTo(outputStream);
		}
	}

	protected void convertToNonPdfFormat(Format format) throws IOException, PrintingException {
		switch (format) {
			case PDF:
				break;
			case PNG:
			case JPG:
				/** instance of PdfDecoder to convert PDF into image */
				PdfDecoder decodePdf = new PdfDecoder(true);

				/** set mappings for non-embedded fonts to use */
				PdfDecoder.setFontReplacements(decodePdf);
				decodePdf.useHiResScreenDisplay(true);
				decodePdf.getDPIFactory().setDpi(2 * 72);
				decodePdf.setPageParameters(1, 1);
				try {
					decodePdf.openPdfArray(baos.toByteArray());
					/** get page 1 as an image */
					BufferedImage img = decodePdf.getPageAsImage(1);

					/** close the pdf file */
					decodePdf.closePdfFile();
					baos.reset();
					ImageIO.write(img, format.getExtension(), baos);
				} catch (PdfException e) {
					throw new PrintingException(e, PrintingException.DOCUMENT_RENDER_PROBLEM);
				}
				break;
			default:
				throw new IllegalStateException("Oops, software error, need to support extra format at end of render"
						+ format);
		}
	}

	protected PdfWriter createWriter(Document document) throws DocumentException {
		PdfWriter writer = PdfWriter.getInstance(document, baos);
		// Render in correct colors for transparent rasters
		writer.setRgbTransparencyBlending(true);
		return writer;
	}

	protected PdfContext createContext(PdfWriter writer) {
		PdfContext context = new PdfContext(writer);
		context.initSize(page.getBounds());
		return context;
	}

	protected abstract void renderContent(Document document, PdfWriter writer, PdfContext context)
			throws DocumentException, BadElementException;

}