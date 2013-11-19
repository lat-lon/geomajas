package org.geomajas.internal.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResourceRetriever {

	@Autowired
	private ResourceService resourceService;
	
	public BufferedImage getImage(String href) throws GeomajasException {
		InputStream is = null;
		try {
			Resource resource = resourceService.find(href);
			if (resource != null) {
				is = resource.getInputStream();
			} else {
				// backwards compatibility
				resource = resourceService.find("images/" + href);
				if (null == resource) {
					resource = resourceService.find("image/" + href);
				}
				if (resource != null) {
					is = resource.getInputStream();
				} else {
					is = ClassLoader.getSystemResourceAsStream(href);
				}
			}
			if (is == null) {
				throw new GeomajasException(ExceptionCode.RESOURCE_NOT_FOUND, href);
			}
			return ImageIO.read(is);
		} catch (IOException io) {
			throw new GeomajasException(io, ExceptionCode.RESOURCE_NOT_FOUND, href);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}

