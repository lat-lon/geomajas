package org.geomajas.plugin.printing.component.impl;

import java.awt.Font;

import org.geomajas.plugin.printing.component.PdfContext;
import org.geomajas.plugin.printing.component.PrintComponent;
import org.geomajas.plugin.printing.component.dto.DynamicLegendComponentInfo;
import org.geomajas.plugin.printing.component.dto.LegendComponentInfo;
import org.geomajas.plugin.printing.parser.FontConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.Rectangle;
import com.thoughtworks.xstream.annotations.XStreamConverter;

@Component()
@Scope(value = "prototype")
public class DynamicLegendComponentImpl extends AbstractLegendComponentImpl<DynamicLegendComponentInfo> {

	private static final float MARGIN = 10;

	/** The font for the text. */
	@XStreamConverter(FontConverter.class)
	private Font font = new Font(LegendComponentInfo.DEFAULT_LEGEND_FONT_FAMILY, Font.PLAIN,
			LegendComponentInfo.DEFAULT_LEGEND_FONTSIZE); // Default font

	public DynamicLegendComponentImpl() {
		this("Legende");
	}

	public DynamicLegendComponentImpl(String title) {
		super(title);
	}

	@Override
	public void layout(PdfContext context) {
		titleLabel.calculateSize(context);

		Rectangle bounds = getBounds();
		float width = bounds.getWidth();
		float height = bounds.getHeight();

		Rectangle titleLabelBounds = titleLabel.getBounds();
		float titleLabelHeight = titleLabelBounds.getHeight();
		float titleLabelWidth = titleLabelBounds.getWidth();
		float labelUrx = width / 2 - titleLabelWidth / 2;
		float labelUry = height - MARGIN;
		float labelLly = labelUry - titleLabelHeight;
		titleLabel.setBounds(new Rectangle(0f, labelLly, labelUrx, labelUry));

		float startHeight = height - titleLabelHeight - MARGIN;

		float currentWidth = MARGIN;
		float currentHeight = startHeight;
		float maxWidth = 0;

		for (PrintComponent<?> child : getChildren()) {
			if (child != titleLabel) {
				child.layout(context);
				Rectangle childBounds = child.getBounds();
				float childWidth = childBounds.getWidth();
				float childHeight = child.getBounds().getHeight();

				maxWidth = Math.max(maxWidth, childWidth);

				currentHeight -= childHeight;
				if (currentHeight < MARGIN) {
					currentWidth += maxWidth;
					currentHeight = startHeight;
					maxWidth = childWidth;
					float llx = currentWidth;
					float urx = currentWidth + childWidth;
					float lly = currentHeight - childHeight;
					float ury = currentHeight;
					child.setBounds(new Rectangle(llx, lly, urx, ury));
					if (currentHeight - childHeight < MARGIN) {
						currentWidth += maxWidth;
					}
				} else {
					float llx = currentWidth;
					float urx = currentWidth + childWidth;
					float lly = currentHeight;
					float ury = currentHeight + childHeight;
					child.setBounds(new Rectangle(llx, lly, urx, ury));
				}
			}
		}
	}

}