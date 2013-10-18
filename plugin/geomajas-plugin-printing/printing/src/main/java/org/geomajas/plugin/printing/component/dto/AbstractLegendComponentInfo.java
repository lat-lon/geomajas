package org.geomajas.plugin.printing.component.dto;

import org.geomajas.configuration.FontStyleInfo;

public abstract class AbstractLegendComponentInfo extends PrintComponentInfo {

	private static final long serialVersionUID = 200L;
	
	/**
	 * Default font family for text in the legend component. Can be overridden by specifying the font via setFont()
	 * 
	 * @since 2.4.0
	 */
	public static final String DEFAULT_LEGEND_FONT_FAMILY = "Dialog";

	/**
	 * Default title for the legend component.
	 * 
	 * @since 2.4.0
	 */
	public static final String DEFAULT_LEGEND_TITLE = "Legende"; // Default is english

	/**
	 * Default font family for text in the legend component. Can be overridden by specifying the font via setFont()
	 * 
	 * @since 2.4.0
	 */
	public static final int DEFAULT_LEGEND_FONTSIZE = 10;

	protected static final String LEGEND_FONT_STYLE_AS_STRING = "Plain";

	private String applicationId;

	private String mapId;

	private FontStyleInfo font;

	/**
	 * Title or Heading text of the legend
	 */
	private String title = DEFAULT_LEGEND_TITLE;

	public AbstractLegendComponentInfo() {
		font = new FontStyleInfo();
		font.setFamily(DEFAULT_LEGEND_FONT_FAMILY);
		font.setStyle(LEGEND_FONT_STYLE_AS_STRING);
		font.setSize(12);
		setFont(font);
	}

	/**
	 * Get application id.
	 * 
	 * @return application id
	 */
	public String getApplicationId() {
		return applicationId;
	}

	/**
	 * Set application id.
	 * 
	 * @param applicationId
	 *            application id
	 */
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	/**
	 * Get map id.
	 * 
	 * @return map id
	 */
	public String getMapId() {
		return mapId;
	}

	/**
	 * Set map id.
	 * 
	 * @param mapId
	 *            map id
	 */
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	/**
	 * Get font style.
	 * 
	 * @return font style
	 */
	public FontStyleInfo getFont() {
		return font;
	}

	/**
	 * Set font style.
	 * 
	 * @param font
	 *            font style
	 */
	public void setFont(FontStyleInfo font) {
		this.font = font;
	}

	/**
	 * Get title.
	 * 
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set title.
	 * 
	 * @param title
	 *            title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
