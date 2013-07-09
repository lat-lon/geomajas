package org.geomajas.command.render;

import java.util.HashMap;

import org.geomajas.command.Command;
import org.geomajas.command.dto.GetMapTilesRequest;
import org.geomajas.command.dto.GetMapTilesResponse;
import org.geomajas.configuration.NamedStyleInfo;
import org.geomajas.layer.MapLayerService;
import org.geomajas.service.DtoConverterService;
import org.geomajas.service.GeoService;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetMapTilesCommand implements Command<GetMapTilesRequest, GetMapTilesResponse> {

	@Autowired
	private DtoConverterService converterService;

	@Autowired
	private MapLayerService mapLayerService;

	@Autowired
	private GeoService geoService;

	@Override
	public GetMapTilesResponse getEmptyCommandResponse() {
		return new GetMapTilesResponse();
	}

	@Override
	public void execute(GetMapTilesRequest request, GetMapTilesResponse response) throws Exception {
		response.setRasterData(mapLayerService.getTiles(request.getVisibleLayers(), new HashMap<String, Filter>(),
				new HashMap<String, NamedStyleInfo>(), geoService.getCrs2(request.getCrs()), converterService.toInternal(request.getBbox()),
				request.getScale()));
	}

}
