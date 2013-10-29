package org.geomajas.plugin.printing.component.impl;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geomajas.layer.Layer;
import org.geomajas.plugin.printing.component.PdfContext;
import org.geomajas.plugin.printing.component.PrintComponent;
import org.geomajas.plugin.printing.component.dto.DynamicLegendComponentInfo;
import org.geomajas.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.Rectangle;

@Component()
@Scope(value = "prototype")
public class DynamicLegendComponentImpl extends AbstractLegendComponentImpl<DynamicLegendComponentInfo> {

	@Autowired
	private ConfigurationService configurationService;

	static final float MARGIN = 10;

	public DynamicLegendComponentImpl() {
		this("Legende");
	}

	public DynamicLegendComponentImpl(String title) {
		super(title);
	}

	@Override
	public void layout(PdfContext context) {
		layoutOnMultiplePages(context);
	}

	@SuppressWarnings("deprecation")
	public List<List<PrintComponent<?>>> layoutOnMultiplePages(PdfContext context) {
		List<List<PrintComponent<?>>> allPageChilds = new ArrayList<List<PrintComponent<?>>>();
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
		float availableHeight = startHeight - MARGIN;

		float currentWidth = MARGIN;
		float currentHeight = startHeight;
		float currentColumnWidth = 0;

		List<PrintComponent<?>> currentPageChilds = new ArrayList<PrintComponent<?>>();
		for (Entry<PrintComponent<?>, Float> childEntry : retrieveChildsSortedByHeight()) {
			PrintComponent<?> child = childEntry.getKey();
			if (child != titleLabel) {
				if (child instanceof LegendViaUrlComponentImpl) {
					String serverLayerId = ((LegendViaUrlComponentImpl) child).getServerLayerId();
					Layer<?> layer = configurationService.getLayer(serverLayerId);
					// if (layer instanceof WmsLayer) {
					// WmsLayer wmsLayer = (WmsLayer) layer;
					// TODO: Further usage of wmsLayer still has to be implemented.
					// It can be used to check if at least one feature is in the envelope.
					// }
				}
				child.layout(context);
				Rectangle childBounds = child.getBounds();
				float childWidth = childBounds.getWidth();
				float childHeight = childBounds.getHeight();

				currentHeight -= childHeight;
				if (isFirstInColumnAndDoesNotFitInColumn(startHeight, currentHeight, childHeight)) {
					float scaleFactor = calculateScaleFactor(availableHeight, childHeight);
					if (maxPageWidthIsAchieved(width, currentWidth, childWidth * scaleFactor)) {
						currentWidth = MARGIN;
						allPageChilds.add(currentPageChilds);
						currentPageChilds = new ArrayList<PrintComponent<?>>();
					}
					currentHeight = startHeight;
					setNewBounds(child, currentWidth, currentHeight, childWidth, childHeight, scaleFactor);
					currentWidth += (childWidth * scaleFactor);
					currentColumnWidth = 0;
				} else if (doesNotFitInColumn(currentHeight)) {
					currentWidth += currentColumnWidth;
					if (maxPageWidthIsAchieved(width, currentWidth, childWidth)) {
						currentWidth = MARGIN;
						allPageChilds.add(currentPageChilds);
						currentPageChilds = new ArrayList<PrintComponent<?>>();
					}
					currentHeight = startHeight - childHeight;
					currentColumnWidth = childWidth;
					setNewBounds(child, currentWidth, currentHeight, childWidth, childHeight);
				} else {
					currentColumnWidth = max(currentColumnWidth, childWidth);
					setNewBounds(child, currentWidth, currentHeight, childWidth, childHeight);
				}
				currentPageChilds.add(child);
			}
		}
		allPageChilds.add(currentPageChilds);
		return allPageChilds;
	}

	private void setNewBounds(PrintComponent<?> child, float currentWidth, float currentHeight, float childWidth,
			float childHeight, float scale) {
		float llx = currentWidth;
		float urx = currentWidth + (childWidth * scale);
		float lly = currentHeight - (childHeight * scale);
		float ury = currentHeight;
		child.setBounds(new Rectangle(llx, lly, urx, ury));
	}

	private void setNewBounds(PrintComponent<?> child, float currentWidth, float currentHeight, float childWidth,
			float childHeight) {
		float llx = currentWidth;
		float urx = currentWidth + childWidth;
		float lly = currentHeight;
		float ury = currentHeight + childHeight;
		child.setBounds(new Rectangle(llx, lly, urx, ury));
	}

	private boolean isFirstInColumnAndDoesNotFitInColumn(float startHeight, float currentHeight, float childHeight) {
		return (currentHeight + childHeight) == startHeight && doesNotFitInColumn(currentHeight);
	}

	private boolean doesNotFitInColumn(float currentHeight) {
		return currentHeight < MARGIN;
	}

	private boolean maxPageWidthIsAchieved(float width, float currentWidth, float childWidth) {
		return currentWidth + childWidth > width - MARGIN;
	}

	private float calculateScaleFactor(float currentHeight, float childHeight) {
		float scaleInPercent = currentHeight / childHeight;
		if (scaleInPercent > 1)
			scaleInPercent = 1;
		return scaleInPercent;
	}

	private List<Entry<PrintComponent<?>, Float>> retrieveChildsSortedByHeight() {
		Map<PrintComponent<?>, Float> child2Height = retrieveChild2HeightMap();
		return sortByHeight(child2Height);
	}

	@SuppressWarnings("deprecation")
	private Map<PrintComponent<?>, Float> retrieveChild2HeightMap() {
		Map<PrintComponent<?>, Float> child2Height = new HashMap<PrintComponent<?>, Float>();
		for (PrintComponent<?> child : getChildren()) {
			if (child != titleLabel) {
				Rectangle childBounds = child.getBounds();
				float childHeight = childBounds.getHeight();
				child2Height.put(child, childHeight);
			}
		}
		return child2Height;
	}

	private List<Entry<PrintComponent<?>, Float>> sortByHeight(Map<PrintComponent<?>, Float> child2Height) {
		Set<Entry<PrintComponent<?>, Float>> entrySet = child2Height.entrySet();
		List<Entry<PrintComponent<?>, Float>> arrayList = new ArrayList<Entry<PrintComponent<?>, Float>>(entrySet);
		Collections.sort(arrayList, new Comparator<Map.Entry<PrintComponent<?>, Float>>() {

			public int compare(Map.Entry<PrintComponent<?>, Float> entry1, Map.Entry<PrintComponent<?>, Float> entry2) {
				return (entry2.getValue()).compareTo(entry1.getValue());
			}
		});
		return arrayList;
	}

	public LabelComponentImpl getTitleLabel() {
		return titleLabel;
	}
}