package org.geomajas.plugin.printing.component.impl;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

		List<Entry<PrintComponent<?>, Float>> sortedListOfChildren = retrieveSortedListOfChildren();
		for (Entry<PrintComponent<?>, Float> childEntry : sortedListOfChildren) {
			PrintComponent<?> child = childEntry.getKey();
			if (child != titleLabel) {
				child.layout(context);
				Rectangle childBounds = child.getBounds();
				float childWidth = childBounds.getWidth();
				float childHeight = childBounds.getHeight();

				maxWidth = Math.max(maxWidth, childWidth);

				currentHeight -= childHeight;
				if (currentHeight < MARGIN) {
					if ((currentHeight + childHeight) != startHeight)
						currentWidth += maxWidth;
					currentHeight = startHeight;
					maxWidth = childWidth;
					float resizeInPercentage = currentHeight / childHeight;
					float llx = currentWidth;
					float urx = currentWidth + (childWidth * resizeInPercentage);
					float lly = MARGIN;
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

	private List<Entry<PrintComponent<?>, Float>> retrieveSortedListOfChildren() {
		Map<PrintComponent<?>, Float> map = retrieveMapWithChildren();
		return retrieveSortedList(map);
	}

	private Map<PrintComponent<?>, Float> retrieveMapWithChildren() {
		Map<PrintComponent<?>, Float> map = new HashMap<PrintComponent<?>, Float>();
		for (PrintComponent<?> child : getChildren()) {
			if (child != titleLabel) {
				Rectangle childBounds = child.getBounds();
				float childHeight = childBounds.getHeight();
				map.put(child, childHeight);
			}
		}
		return map;
	}

	private List<Entry<PrintComponent<?>, Float>> retrieveSortedList(Map<PrintComponent<?>, Float> map) {
		Set<Entry<PrintComponent<?>, Float>> entrySet = map.entrySet();
		List<Entry<PrintComponent<?>, Float>> arrayList = new ArrayList<Entry<PrintComponent<?>, Float>>(entrySet);
		Collections.sort(arrayList, new Comparator<Map.Entry<PrintComponent<?>, Float>>() {

			public int compare(Map.Entry<PrintComponent<?>, Float> entry1, Map.Entry<PrintComponent<?>, Float> entry2) {
				return (entry2.getValue()).compareTo(entry1.getValue());
			}
		});
		return arrayList;
	}

}
