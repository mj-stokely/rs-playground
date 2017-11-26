#pragma version(1)
#pragma rs java_package_name(com.mystokely.rsplayground)

rs_allocation inAllocation;
int32_t yApplyUntil = 0;
int32_t yApplyAfter = 10000;

const static float3 grayMultipliers = {0.299f, 0.587f, 0.114f};

uchar4 RS_KERNEL root(uint32_t x, uint32_t y){
  if(y >= yApplyUntil && y <= yApplyAfter){
    return rsGetElementAt_uchar4(inAllocation, x, y);
  }

  // blackout pixel
  uchar4 pixelOut;
  pixelOut.r = 0;
  pixelOut.g = 0;
  pixelOut.b = 0;
  pixelOut.a = 255;
  return pixelOut;
}