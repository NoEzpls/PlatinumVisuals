package dev.platinum.visuals;

public final class MotionChecks {
  public static void main(String[] args){
    float expected=1-(float)Math.exp(-20*.2);
    for(int fps:new int[]{30,60,120,144,240}){
      float x=0;double elapsed=0;
      while(elapsed<.2-1e-10){double dt=Math.min(1.0/fps,.2-elapsed);float before=x;x=Motion.approach(x,1,20,dt);if(x<before||x>1)throw new AssertionError("overshoot");elapsed+=dt;}
      if(Math.abs(x-expected)>.0001)throw new AssertionError("timing depends on FPS: "+fps);
    }
    float x=Motion.approach(.5f,0,20,.016);if(x<0||x>.5)throw new AssertionError("reverse");
    if(Motion.approach(.4f,1,20,Double.NaN)!=.4f)throw new AssertionError("NaN");
    if(Motion.approach(.4f,1,20,-1)!=.4f)throw new AssertionError("negative delta");
    System.out.println("Motion: consistent at 30/60/120/144/240 FPS; bounded on reversal and invalid deltas.");
  }
}
