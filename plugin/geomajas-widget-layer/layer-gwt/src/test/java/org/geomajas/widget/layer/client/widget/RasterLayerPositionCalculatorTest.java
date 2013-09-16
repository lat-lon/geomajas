package org.geomajas.widget.layer.client.widget;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;

/**
 * 
 * @author Dirk Stenger
 * @author Lyn Goltz
 * 
 */
public class RasterLayerPositionCalculatorTest {

	private RasterLayerPositionCalculator layerPositionCalculator = new RasterLayerPositionCalculator();

	@Test
	public void testCalculatePositionWithEmptyNonRasterLayersListFirstLayer() {
		int requestedLayerPosition = 0;
		ArrayList<Integer> emptyNonRasterLayersList = new ArrayList<Integer>();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition,
				emptyNonRasterLayersList);
		assertThat(calculatedPosition, is(requestedLayerPosition));
	}

	@Test
	public void testCalculatePositionWithEmptyNonRasterLayersListSixthLayer() {
		int requestedLayerPosition = 5;
		ArrayList<Integer> emptyNonRasterLayersList = new ArrayList<Integer>();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition,
				emptyNonRasterLayersList);
		assertThat(calculatedPosition, is(requestedLayerPosition));
	}

	@Test
	public void testCalculatePositionWithOneNonRasterLayerBeforeSixthLayer() {
		int requestedLayerPosition = 5;
		int onePositionShifted = requestedLayerPosition + 1;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithOneLayerAtTheBeginning();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(onePositionShifted));
	}

	@Test
	public void testCalculatePositionWithOneNonRasterLayerAfterSixthLayer() {
		int requestedLayerPosition = 5;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithOneLayerAtSeventhPosition();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(requestedLayerPosition));
	}

	@Test
	public void testCalculatePositionWithOneNonRasterLayerAtSamePositionAsRequestedLayer() {
		int requestedLayerPosition = 6;
		int onePositionShifted = requestedLayerPosition + 1;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithOneLayerAtSeventhPosition();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(onePositionShifted));
	}

	@Test
	public void testCalculatePositionWithTwoNonRasterLayersBeforeSixthLayer() {
		int requestedLayerPosition = 5;
		int twoPositionsShifted = requestedLayerPosition + 2;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithTwoLayerAtTheBeginning();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(twoPositionsShifted));
	}

	@Test
	public void testCalculatePositionWithTwoNonRasterLayersAfterSixthLayer() {
		int requestedLayerPosition = 5;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithTwoLayersAtTenthAndEleventhPosition();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(requestedLayerPosition));
	}

	@Test
	public void testCalculatePositionWithTwoNonRasterLayersBeforeAndAfterSixthLayer() {
		int requestedLayerPosition = 5;
		int twoPositionsShifted = requestedLayerPosition + 2;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithFourLayersAtFirstAndSecondAndTenthAndEleventhPosition();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(twoPositionsShifted));
	}

	@Test
	public void testCalculatePositionWithThreeNonRasterLayersAtFirstAndSecondAndSeventhPosition() {
		int requestedLayerPosition = 5;
		int twoPositionsShifted = requestedLayerPosition + 3;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithTwoLayersAtFirstAndSecondAndSeventhPosition();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(twoPositionsShifted));
	}

	@Test
	public void testCalculatePositionWithThreeNonRasterLayersBeforeSecondLayer() {
		int requestedLayerPosition = 1;
		int twoPositionsShifted = requestedLayerPosition + 3;
		ArrayList<Integer> nonRasterLayersList = createNonRasterLayersListWithThreeLayersAtTheBeginning();
		int calculatedPosition = layerPositionCalculator.calculatePosition(requestedLayerPosition, nonRasterLayersList);
		assertThat(calculatedPosition, is(twoPositionsShifted));
	}

	@Test(expected = NullPointerException.class)
	public void testCalculatePositionWithNullNonRasterLayersList() {
		layerPositionCalculator.calculatePosition(0, null);
	}

	private ArrayList<Integer> createNonRasterLayersListWithOneLayerAtTheBeginning() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(0);
		return nonRasterLayersList;
	}

	private ArrayList<Integer> createNonRasterLayersListWithOneLayerAtSeventhPosition() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(6);
		return nonRasterLayersList;
	}

	private ArrayList<Integer> createNonRasterLayersListWithTwoLayerAtTheBeginning() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(0);
		nonRasterLayersList.add(1);
		return nonRasterLayersList;
	}

	private ArrayList<Integer> createNonRasterLayersListWithTwoLayersAtTenthAndEleventhPosition() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(9);
		nonRasterLayersList.add(10);
		return nonRasterLayersList;
	}

	private ArrayList<Integer> createNonRasterLayersListWithFourLayersAtFirstAndSecondAndTenthAndEleventhPosition() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(0);
		nonRasterLayersList.add(1);
		nonRasterLayersList.add(9);
		nonRasterLayersList.add(10);
		return nonRasterLayersList;
	}

	private ArrayList<Integer> createNonRasterLayersListWithTwoLayersAtFirstAndSecondAndSeventhPosition() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(0);
		nonRasterLayersList.add(1);
		nonRasterLayersList.add(6);
		return nonRasterLayersList;
	}

	private ArrayList<Integer> createNonRasterLayersListWithThreeLayersAtTheBeginning() {
		ArrayList<Integer> nonRasterLayersList = new ArrayList<Integer>();
		nonRasterLayersList.add(0);
		nonRasterLayersList.add(1);
		nonRasterLayersList.add(2);
		return nonRasterLayersList;
	}

}
