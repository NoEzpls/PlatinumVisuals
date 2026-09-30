package dev.platinum.visuals;

/** The drawing transform and its exact inverse are kept in the same value. */
public record UiViewport(float scale, float left, float top) {
  public static UiViewport centered(int width,int height,float logicalWidth,float logicalHeight,float requested) {
    float scale=Math.max(.05f,Math.min(requested,Math.min((width-20f)/logicalWidth,(height-20f)/logicalHeight)));
    return new UiViewport(scale,(width-logicalWidth*scale)/2f,(height-logicalHeight*scale)/2f);
  }
  public double x(double screenX){return (screenX-left)/scale;}
  public double y(double screenY){return (screenY-top)/scale;}
  public double screenX(double x){return left+x*scale;}
  public double screenY(double y){return top+y*scale;}
  public record Rect(float x,float y,float w,float h) {
    public boolean contains(double px,double py){return px>=x&&py>=y&&px<x+w&&py<y+h;}
  }
}
