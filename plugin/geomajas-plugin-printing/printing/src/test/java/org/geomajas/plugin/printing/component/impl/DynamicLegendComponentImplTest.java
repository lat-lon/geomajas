package org.geomajas.plugin.printing.component.impl;

import static java.lang.Math.abs;
import static org.geomajas.plugin.printing.component.impl.DynamicLegendComponentImpl.MARGIN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Font;
import java.util.List;

import org.geomajas.plugin.printing.component.PdfContext;
import org.geomajas.plugin.printing.component.PrintComponent;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.lowagie.text.Rectangle;

/**
 * Description of the test scenarios:
 * 
 * <pre>
 * Scenario 1:
 * 
 * Tests using this scenario: 
 *  * testLayoutOnMultiplePagesWithOneLegendFittingOnOnePageAssertCorrectPageNumber
 *  * testLayoutOnMultiplePagesWithOneLegendFittingOnOnePageAssertCorrectChildBounds
 * 
 * Page dimension: 100 x 100
 * 
 *  _______ 100
 * | |                
 * |_|_____ 73
 * | |1 |   
 * | |__|   
 * | |
 * 
 *   10 14
 * 
 * Legend dimensions (width x height):
 *  1: 4x5
 * </pre>
 * 
 * <pre>
 * Scenario 2:
 * 
 * Tests using this scenario: 
 *  * testLayoutOnMultiplePagesWithMultipleLegendsFittingOnOnePageAssertCorrectPageNumber
 *  * testLayoutOnMultiplePagesWithMultipleLegendsFittingOnOnePageAssertCorrectChildBounds
 * 
 * Page dimension: 100 x 100
 * 
 *  ____________________  100
 * | |                | |
 * |_|________________|_|  73
 * | |    |   | 2 |   | |
 * | | 3  | 1 |___|   | |
 * | |    |___|       | |
 * | |____|4 |        | |  
 * | |    |__|        | |
 * | |                | |
 * |_|________________|_|   0
 *   10   35 55   75 90 100
 * 
 * Legend dimensions (width x height):
 *  1: 20x35
 *  2: 20x20
 *  3: 25x50
 *  4: 13x23
 * </pre>
 * 
 * <pre>
 * Scenario 3:
 * 
 * Tests using this scenario: 
 *  * testLayoutOnMultiplePagesWithMultipleLegendsOneToHeightFittingOnOnePageAssertCorrectPageNumber
 *  * testLayoutOnMultiplePagesWithMultipleLegendsOneToHeightFittingOnOnePageAssertCorrectChildBounds
 * 
 * Page dimension: 100 x 100
 * 
 *  ____________________  100
 * | |                | |
 * |_|________________|_|  73
 * | |   |   | 3|     | |
 * | | 2 | 1 |__|     | |
 * | |   |   |        | |
 * | |   |   |        | |  
 * | |   |___|        | |
 * | |___|            | |
 * |_|________________|_|   
 *   10   30 55   75 90 100
 * 
 * Legend dimensions (width x height):
 *  1: 25x60
 *  2: 20x126
 *  3: 14x20
 * 
 * </pre>
 * 
 * <pre>
 * Scenario 4:
 * 
 * Tests using this scenario: 
 *  * testLayoutOnMultiplePagesWithMultipleLegendsFittingOnTwoPagesAssertCorrectPageNumber
 *  * testLayoutOnMultiplePagesWithMultipleLegendsFittingOnTwoPagesAssertCorrectPageNumber
 * 
 * Page dimension: 100 x 100
 * 
 *  ____________________  100
 * | |                | |
 * |_|________________|_|  73
 * | |      |     |   | |
 * | |  1   |  2  |   | |
 * | |      |     |   | |
 * | |      |     |   | |  
 * | |      |_____|   | |
 * | |______|         | |
 * |_|________________|_|   
 *   10     45   82  90 100
 * 
 *  ____________________  100
 * | |                | |
 * |_|________________|_|  73
 * | |   |            | |
 * | | 3 |            | |
 * | |   |            | |
 * | |___|            | |  
 * | |                | |
 * | |                | |
 * |_|________________|_|   
 *   10  20         90 100
 *   
 * Legend dimensions (width x height):
 *  1: 25x60
 *  2: 20x126
 *  3: 14x20
 * 
 * </pre>
 * 
 * <pre>
 * Scenario 5:
 * 
 * Tests using this scenario: 
 *  * testLayoutOnMultiplePagesWithMultipleLegendsTooHeightFittingOnTwoPagesAssertCorrectPageNumber
 *  * testLayoutOnMultiplePagesWithMultipleLegendsTooHeightFittingOnTwoPagesAssertCorrectChildBounds
 * 
 * Page dimension: 100 x 100
 * 
 *  ____________________  100
 * | |                | |
 * |_|________________|_|  73
 * | |      |     |   | |
 * | |  1   |  2  |   | |
 * | |      |     |   | |
 * | |      |     |   | |  
 * | |      |     |   | |
 * | |      |     |   | |
 * |_|______|_____|___|_|   
 *   10             90 100
 * 
 *   ____________________  100
 * | |                | |
 * |_|________________|_|  73
 * | |      |         | |
 * | |  3   |         | |
 * | |      |         | |
 * | |      |         | |  
 * | |      |         | |
 * | |      |         | |
 * |_|______|_________|_|   
 *   10             90 100
 *   
 * Legend dimensions (width x height):
 *  1:160x252
 *  2: 120x189
 *  3: 80x126
 * 
 * </pre>
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class DynamicLegendComponentImplTest {

	private static final int PAGE_WIDTH = 100;

	private static final int PAGE_HEIGHT = 100;

	private static final int TITLE_LABEL_MARGIN = 10;

	private static final int TITLE_LABEL_HEIGHT = 7;

	private static final int TITLE_LABEL_WIDTH = 20;

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithOneLegendFittingOnOnePageAssertCorrectPageNumber() throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));

		int childWidth = 4;
		int childHeight = 5;
		PrintComponent<?> mockedChild = mockChild(childWidth, childHeight);
		legendComponent.addComponent(mockedChild);

		List<List<PrintComponent<?>>> layoutOnMultiplePages = legendComponent.layoutOnMultiplePages(mockContext());
		assertThat(layoutOnMultiplePages.size(), is(1));

		List<PrintComponent<?>> childsOnPageOne = layoutOnMultiplePages.get(0);
		assertThat(childsOnPageOne.size(), is(1));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithOneLegendFittingOnOnePageAssertCorrectChildBounds() throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));

		int childWidth = 4;
		int childHeight = 5;
		PrintComponent<?> mockedChild = mockChild(childWidth, childHeight);
		legendComponent.addComponent(mockedChild);

		legendComponent.layoutOnMultiplePages(mockContext());

		ArgumentCaptor<Rectangle> bounds = ArgumentCaptor.forClass(Rectangle.class);
		verify(mockedChild).setBounds(bounds.capture());
		Rectangle boundsOfTheFirstChildOnPageOne = bounds.getValue();

		assertThat(boundsOfTheFirstChildOnPageOne.getLeft(), is(MARGIN));
		assertThat(boundsOfTheFirstChildOnPageOne.getRight(), is(MARGIN + childWidth));

		assertThat(boundsOfTheFirstChildOnPageOne.getTop(), is(calculateMarginFromTopToFirstChild()));
		assertThat(boundsOfTheFirstChildOnPageOne.getBottom(), is(calculateMarginFromTopToFirstChild() - childHeight));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsFittingOnOnePageAssertCorrectPageNumber() throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		legendComponent.addComponent(mockChild(20, 35));
		legendComponent.addComponent(mockChild(20, 20));
		legendComponent.addComponent(mockChild(25, 50));
		legendComponent.addComponent(mockChild(13, 23));

		List<List<PrintComponent<?>>> layoutOnMultiplePages = legendComponent.layoutOnMultiplePages(context);
		assertThat(layoutOnMultiplePages.size(), is(1));

		List<PrintComponent<?>> childsOnPageOne = layoutOnMultiplePages.get(0);
		assertThat(childsOnPageOne.size(), is(4));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsFittingOnOnePageAssertCorrectChildBounds() throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		PrintComponent<?> mockedChild1 = mockChild(20, 35);
		PrintComponent<?> mockedChild2 = mockChild(20, 20);
		PrintComponent<?> mockedChild3 = mockChild(25, 50);
		PrintComponent<?> mockedChild4 = mockChild(13, 23);

		legendComponent.addComponent(mockedChild1);
		legendComponent.addComponent(mockedChild2);
		legendComponent.addComponent(mockedChild3);
		legendComponent.addComponent(mockedChild4);

		legendComponent.layoutOnMultiplePages(context);

		float marginFromLeftToColumn1 = MARGIN;
		float marginFromLeftToColumn2 = marginFromLeftToColumn1 + 25;
		float marginFromLeftToColumn3 = marginFromLeftToColumn2 + 20;
		float marginFromTopToFirstChild = calculateMarginFromTopToFirstChild();

		ArgumentCaptor<Rectangle> bounds = ArgumentCaptor.forClass(Rectangle.class);
		verify(mockedChild1).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild1 = bounds.getAllValues().get(0);
		Rectangle boundsOfChild1 = mockedChild1.getBounds();
		assertThat(newBoundsOfTheChild1, isLeft(marginFromLeftToColumn2));
		assertThat(newBoundsOfTheChild1, isRight(marginFromLeftToColumn2 + boundsOfChild1.getWidth()));
		assertThat(newBoundsOfTheChild1, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild1, isBottom(marginFromTopToFirstChild - boundsOfChild1.getHeight()));

		verify(mockedChild2).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild2 = bounds.getAllValues().get(1);
		Rectangle boundsOfChild2 = mockedChild2.getBounds();
		assertThat(newBoundsOfTheChild2, isLeft(marginFromLeftToColumn3));
		assertThat(newBoundsOfTheChild2, isRight(marginFromLeftToColumn3 + boundsOfChild2.getWidth()));
		assertThat(newBoundsOfTheChild2, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild2, isBottom(marginFromTopToFirstChild - boundsOfChild2.getHeight()));

		verify(mockedChild3).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild3 = bounds.getAllValues().get(2);
		Rectangle boundsOfChild3 = mockedChild3.getBounds();
		assertThat(newBoundsOfTheChild3, isLeft(marginFromLeftToColumn1));
		assertThat(newBoundsOfTheChild3, isRight(marginFromLeftToColumn1 + boundsOfChild3.getWidth()));
		assertThat(newBoundsOfTheChild3, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild3, isBottom(marginFromTopToFirstChild - boundsOfChild3.getHeight()));

		verify(mockedChild4).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild4 = bounds.getAllValues().get(3);
		Rectangle boundsOfChild4 = mockedChild4.getBounds();
		float marginFromTopToChild4 = marginFromTopToFirstChild - 35;
		assertThat(newBoundsOfTheChild4, isLeft(marginFromLeftToColumn2));
		assertThat(newBoundsOfTheChild4, isRight(marginFromLeftToColumn2 + boundsOfChild4.getWidth()));
		assertThat(newBoundsOfTheChild4, isTop(marginFromTopToChild4));
		assertThat(newBoundsOfTheChild4, isBottom(marginFromTopToChild4 - boundsOfChild4.getHeight()));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsOneToHeightFittingOnOnePageAssertCorrectPageNumber()
			throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		legendComponent.addComponent(mockChild(25, 60));
		legendComponent.addComponent(mockChild(20, 90));
		legendComponent.addComponent(mockChild(14, 20));

		List<List<PrintComponent<?>>> layoutOnMultiplePages = legendComponent.layoutOnMultiplePages(context);
		assertThat(layoutOnMultiplePages.size(), is(1));

		List<PrintComponent<?>> childsOnPageOne = layoutOnMultiplePages.get(0);
		assertThat(childsOnPageOne.size(), is(3));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsOneToHeightFittingOnOnePageAssertCorrectChildBounds()
			throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		PrintComponent<?> mockedChild1 = mockChild(25, 60);
		PrintComponent<?> mockedChild2 = mockChild(20, 126);
		PrintComponent<?> mockedChild3 = mockChild(14, 20);

		legendComponent.addComponent(mockedChild1);
		legendComponent.addComponent(mockedChild2);
		legendComponent.addComponent(mockedChild3);

		legendComponent.layoutOnMultiplePages(context);

		float marginFromLeftToColumn1 = MARGIN;
		float marginFromLeftToColumn2 = marginFromLeftToColumn1 + 10;
		float marginFromLeftToColumn3 = marginFromLeftToColumn2 + 25;
		float marginFromTopToFirstChild = calculateMarginFromTopToFirstChild();

		ArgumentCaptor<Rectangle> bounds = ArgumentCaptor.forClass(Rectangle.class);
		verify(mockedChild1).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild1 = bounds.getAllValues().get(0);
		Rectangle boundsOfChild1 = mockedChild1.getBounds();
		assertThat(newBoundsOfTheChild1, isLeft(marginFromLeftToColumn2));
		assertThat(newBoundsOfTheChild1, isRight(marginFromLeftToColumn2 + boundsOfChild1.getWidth()));
		assertThat(newBoundsOfTheChild1, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild1, isBottom(marginFromTopToFirstChild - boundsOfChild1.getHeight()));

		verify(mockedChild2).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild2 = bounds.getAllValues().get(1);
		Rectangle boundsOfChild2 = mockedChild2.getBounds();
		assertThat(newBoundsOfTheChild2, isLeft(marginFromLeftToColumn1));
		assertThat(newBoundsOfTheChild2, isRight(marginFromLeftToColumn1 + (boundsOfChild2.getWidth() / 2)));
		assertThat(newBoundsOfTheChild2, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild2, isBottom(MARGIN));

		verify(mockedChild3).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild3 = bounds.getAllValues().get(2);
		Rectangle boundsOfChild3 = mockedChild3.getBounds();
		assertThat(newBoundsOfTheChild3, isLeft(marginFromLeftToColumn3));
		assertThat(newBoundsOfTheChild3, isRight(marginFromLeftToColumn3 + boundsOfChild3.getWidth()));
		assertThat(newBoundsOfTheChild3, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild3, isBottom(marginFromTopToFirstChild - boundsOfChild3.getHeight()));

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsFittingOnTwoPagesAssertCorrectPageNumber() throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		legendComponent.addComponent(mockChild(35, 60));
		legendComponent.addComponent(mockChild(37, 55));
		legendComponent.addComponent(mockChild(20, 45));

		List<List<PrintComponent<?>>> layoutOnMultiplePages = legendComponent.layoutOnMultiplePages(context);
		assertThat(layoutOnMultiplePages.size(), is(2));

		List<PrintComponent<?>> childsOnPageOne = layoutOnMultiplePages.get(0);
		assertThat(childsOnPageOne.size(), is(2));

		List<PrintComponent<?>> childsOnPageTwo = layoutOnMultiplePages.get(1);
		assertThat(childsOnPageTwo.size(), is(1));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsFittingOnTwoPagesAssertCorrectChildBounds()
			throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		PrintComponent<?> mockedChild1 = mockChild(35, 60);
		PrintComponent<?> mockedChild2 = mockChild(37, 55);
		PrintComponent<?> mockedChild3 = mockChild(20, 45);

		legendComponent.addComponent(mockedChild1);
		legendComponent.addComponent(mockedChild2);
		legendComponent.addComponent(mockedChild3);

		legendComponent.layoutOnMultiplePages(context);

		float marginFromLeftToColumn1 = MARGIN;
		float marginFromLeftToColumn2 = marginFromLeftToColumn1 + 35;
		float marginFromTopToFirstChild = calculateMarginFromTopToFirstChild();

		ArgumentCaptor<Rectangle> bounds = ArgumentCaptor.forClass(Rectangle.class);
		verify(mockedChild1).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild1 = bounds.getAllValues().get(0);
		Rectangle boundsOfChild1 = mockedChild1.getBounds();
		assertThat(newBoundsOfTheChild1, isLeft(marginFromLeftToColumn1));
		assertThat(newBoundsOfTheChild1, isRight(marginFromLeftToColumn1 + boundsOfChild1.getWidth()));
		assertThat(newBoundsOfTheChild1, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild1, isBottom(marginFromTopToFirstChild - boundsOfChild1.getHeight()));

		verify(mockedChild2).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild2 = bounds.getAllValues().get(1);
		Rectangle boundsOfChild2 = mockedChild2.getBounds();
		assertThat(newBoundsOfTheChild2, isLeft(marginFromLeftToColumn2));
		assertThat(newBoundsOfTheChild2, isRight(marginFromLeftToColumn2 + boundsOfChild2.getWidth()));
		assertThat(newBoundsOfTheChild2, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild2, isBottom(marginFromTopToFirstChild - boundsOfChild2.getHeight()));

		// new page
		verify(mockedChild3).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild3 = bounds.getAllValues().get(2);
		Rectangle boundsOfChild3 = mockedChild3.getBounds();
		assertThat(newBoundsOfTheChild3, isLeft(marginFromLeftToColumn1));
		assertThat(newBoundsOfTheChild3, isRight(marginFromLeftToColumn1 + boundsOfChild3.getWidth()));
		assertThat(newBoundsOfTheChild3, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild3, isBottom(marginFromTopToFirstChild - boundsOfChild3.getHeight()));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsTooHeightFittingOnTwoPagesAssertCorrectPageNumber()
			throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		legendComponent.addComponent(mockChild(160, 252));
		legendComponent.addComponent(mockChild(120, 189));
		legendComponent.addComponent(mockChild(80, 126));

		List<List<PrintComponent<?>>> layoutOnMultiplePages = legendComponent.layoutOnMultiplePages(context);
		assertThat(layoutOnMultiplePages.size(), is(2));

		List<PrintComponent<?>> childsOnPageOne = layoutOnMultiplePages.get(0);
		assertThat(childsOnPageOne.size(), is(2));

		List<PrintComponent<?>> childsOnPageTwo = layoutOnMultiplePages.get(1);
		assertThat(childsOnPageTwo.size(), is(1));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testLayoutOnMultiplePagesWithMultipleLegendsTooHeightFittingOnTwoPagesAssertCorrectChildBounds()
			throws Exception {
		DynamicLegendComponentImpl legendComponent = new DynamicLegendComponentImpl();
		legendComponent.setBounds(new Rectangle(PAGE_WIDTH, PAGE_HEIGHT));
		PdfContext context = mockContext();

		PrintComponent<?> mockedChild1 = mockChild(160, 252);
		PrintComponent<?> mockedChild2 = mockChild(120, 189);
		PrintComponent<?> mockedChild3 = mockChild(80, 126);

		legendComponent.addComponent(mockedChild1);
		legendComponent.addComponent(mockedChild2);
		legendComponent.addComponent(mockedChild3);

		legendComponent.layoutOnMultiplePages(context);

		float marginFromLeftToColumn1 = MARGIN;
		float marginFromLeftToColumn2 = marginFromLeftToColumn1 + 40;
		float marginFromTopToFirstChild = calculateMarginFromTopToFirstChild();

		ArgumentCaptor<Rectangle> bounds = ArgumentCaptor.forClass(Rectangle.class);
		verify(mockedChild1).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild1 = bounds.getAllValues().get(0);
		Rectangle boundsOfChild1 = mockedChild1.getBounds();
		assertThat(newBoundsOfTheChild1, isLeft(marginFromLeftToColumn1));
		assertThat(newBoundsOfTheChild1, isRight(marginFromLeftToColumn1 + (boundsOfChild1.getWidth() / 4)));
		assertThat(newBoundsOfTheChild1, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild1, isBottom(marginFromTopToFirstChild - (boundsOfChild1.getHeight() / 4)));

		verify(mockedChild2).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild2 = bounds.getAllValues().get(1);
		Rectangle boundsOfChild2 = mockedChild2.getBounds();
		assertThat(newBoundsOfTheChild2, isLeft(marginFromLeftToColumn2));
		assertThat(newBoundsOfTheChild2, isRight(marginFromLeftToColumn2 + (boundsOfChild2.getWidth() / 3)));
		assertThat(newBoundsOfTheChild2, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild2, isBottom(marginFromTopToFirstChild - (boundsOfChild2.getHeight() / 3)));

		// new page
		verify(mockedChild3).setBounds(bounds.capture());
		Rectangle newBoundsOfTheChild3 = bounds.getAllValues().get(2);
		Rectangle boundsOfChild3 = mockedChild3.getBounds();
		assertThat(newBoundsOfTheChild3, isLeft(marginFromLeftToColumn1));
		assertThat(newBoundsOfTheChild3, isRight(marginFromLeftToColumn1 + (boundsOfChild3.getWidth() / 2)));
		assertThat(newBoundsOfTheChild3, isTop(marginFromTopToFirstChild));
		assertThat(newBoundsOfTheChild3, isBottom(marginFromTopToFirstChild - (boundsOfChild3.getHeight() / 2)));
	}

	private PdfContext mockContext() {
		PdfContext pdfContextMock = mock(PdfContext.class);
		when(pdfContextMock.getTextSize(Mockito.anyString(), (Font) Mockito.anyObject())).thenReturn(
				new Rectangle(TITLE_LABEL_WIDTH, TITLE_LABEL_HEIGHT));
		return pdfContextMock;
	}

	private PrintComponent<?> mockChild(final int width, final int height) {
		PrintComponent<?> mock = mock(PrintComponent.class);
		when(mock.getBounds()).thenReturn(new Rectangle(width, height));
		return mock;
	}

	private float calculateMarginFromTopToFirstChild() {
		return PAGE_HEIGHT - TITLE_LABEL_HEIGHT - MARGIN - TITLE_LABEL_MARGIN;
	}

	private Matcher<Rectangle> isLeft(final float expectedLeft) {
		return new BaseMatcher<Rectangle>() {

			@Override
			public boolean matches(Object item) {
				Rectangle rectangle = (Rectangle) item;
				return isNearlyTheSame(expectedLeft, rectangle.getLeft());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Expected is lower left x " + expectedLeft);
			}

		};
	}

	private Matcher<Rectangle> isRight(final float expectedRight) {
		return new BaseMatcher<Rectangle>() {

			@Override
			public boolean matches(Object item) {
				Rectangle rectangle = (Rectangle) item;
				return isNearlyTheSame(expectedRight, rectangle.getRight());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Expected is upper left x " + expectedRight);
			}
		};
	}

	private Matcher<Rectangle> isTop(final float expectedTop) {
		return new BaseMatcher<Rectangle>() {

			@Override
			public boolean matches(Object item) {
				Rectangle rectangle = (Rectangle) item;
				return isNearlyTheSame(expectedTop, rectangle.getTop());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Expected is upper left y " + expectedTop);
			}
		};
	}

	private Matcher<Rectangle> isBottom(final float expectedBottom) {
		return new BaseMatcher<Rectangle>() {

			@Override
			public boolean matches(Object item) {
				Rectangle rectangle = (Rectangle) item;
				return isNearlyTheSame(expectedBottom, rectangle.getBottom());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Expected is lower left y " + expectedBottom);
			}
		};
	}

	private boolean isNearlyTheSame(final float value1, float value2) {
		return abs(value2 - value1) < 0.0001;
	}
}