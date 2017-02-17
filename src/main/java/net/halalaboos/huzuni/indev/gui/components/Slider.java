package net.halalaboos.huzuni.indev.gui.components;

import net.halalaboos.huzuni.indev.gui.Component;
import net.halalaboos.huzuni.indev.gui.actions.Actions;
import net.halalaboos.huzuni.indev.gui.actions.ClickAction;

/**
 * Simple slider implementation. <br/>
 * Created by Brandon Williams on 1/15/2017.
 */
public class Slider extends Component {

    private String title;

    private int barSize;

    private float sliderPercentage = 0F;

    private boolean sliding = false;

    public Slider(String tag, String title) {
        this(tag, title, 8);
    }

    public Slider(String tag, String title, int barSize) {
        super(tag);
        this.title = title;
        this.barSize = barSize;
        this.addListener(Actions.MOUSEPRESS, (ClickAction.ClickActionListener) action -> {
            if (isHovered() && isPointInside(action.x, action.y)) {
                sliding = true;
                return true;
            }
            return false;
        });
        this.addListener(Actions.MOUSERELEASE, (ClickAction.ClickActionListener) action -> {
            sliding = false;
            return false;
        });
    }

    @Override
    public void update() {
        updateSliding();
    }

    /**
     * Updates the slider percentage.
     * */
    public void updateSliding() {
        if (this.sliding) {
            this.sliderPercentage = (inputUtility.getMouseX() - getX() - (getBarSize() / 2F)) / (getWidthForSlider());
            if (sliderPercentage > 1)
                sliderPercentage = 1;
            if (sliderPercentage < 0)
                sliderPercentage = 0;
        } else
            this.sliding = false;
    }

    /**
     * @return The area of the bar used by this slider.
     * */
    public int[] getSliderBar() {
        return new int[] { getX() + (int) (sliderPercentage * getWidthForSlider()), getY(), getBarSize(), getHeight() };
    }

    /**
     * @return True if the given point is within the slider bar.
     * */
    public boolean isPointInsideSlider(int x, int y) {
        return inputUtility.isPointInside(x, y, getSliderBar());
    }

    /**
     * @return The total width for the slider.
     * */
    public float getWidthForSlider() {
        int SLIDER_PADDING = 0;
        float maxPointForRendering = (float) (getWidth() - getBarSize() - SLIDER_PADDING),
                beginPoint = (SLIDER_PADDING);
        return maxPointForRendering - beginPoint;
    }

    public boolean isSliding() {
        return sliding;
    }

    public int getBarSize() {
        return barSize;
    }

    public void setBarSize(int barSize) {
        this.barSize = barSize;
    }

    public float getSliderPercentage() {
        return sliderPercentage;
    }

    public void setSliderPercentage(float sliderPercentage) {
        this.sliderPercentage = sliderPercentage;
    }

    public int getFormattedValue() {
        return (int) (sliderPercentage * 100);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
