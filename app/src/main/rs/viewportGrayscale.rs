#pragma version(1)
#pragma rs java_package_name(com.mystokely.rsplayground)

rs_allocation inAllocation;
int32_t yApplyUntil = 0;
int32_t yApplyAfter = 10000;

const static float3 grayMultipliers = {0.299f, 0.587f, 0.114f};

uchar4 RS_KERNEL root(uint32_t x, uint32_t y){
  uchar4 in = rsGetElementAt_uchar4(inAllocation, x, y);
  if(y >= yApplyUntil && y <= yApplyAfter){
    return in;
  }


  // Multiplies each input's pixel RGB values by their respective gray multipliers
  uchar grayValue = (uchar) ((float) in.r * grayMultipliers.r +
                    (float) in.g * grayMultipliers.g +
                    (float) in.b * grayMultipliers.b);

  uchar4 pixelOut;
  pixelOut.r = grayValue;
  pixelOut.g = grayValue;
  pixelOut.b = grayValue;
  pixelOut.a = in.a; // Preserve alpha

  return pixelOut;
}