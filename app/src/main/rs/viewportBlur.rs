#pragma version(1)
#pragma rs java_package_name(com.mystokely.rsplayground)

rs_allocation inAllocation;
int32_t yApplyUntil = 0;
int32_t yApplyAfter = 10000;
short blurRadius = 1;

uchar4 RS_KERNEL blur(uint32_t x, uint32_t y){
  uchar4 in = rsGetElementAt_uchar4(inAllocation, x, y);
  if(y >= yApplyUntil && y <= yApplyAfter){
    return in;
  }

  uint32_t xMax = 1080;//rsAllocationGetDimX(inAllocation);
  uint32_t yMax = 810;//rsAllocationGetDimY(inAllocation);

  uchar4 pixelOut;
  uint32_t r;
  uint32_t g;
  uint32_t b;
  for (short ky = -blurRadius; ky <= blurRadius && ky <= yMax; ++ky){
      //for (short kx = -blurRadius; kx <= blurRadius && kx <= xMax; ++kx){
          if(ky < 0){
            continue;
          }
          //if(kx < 0){
            //continue;
          //}
          uchar4 blurWith = rsGetElementAt_uchar4(inAllocation, x, y+ky);
          r = r + blurWith.r;
          g = g + blurWith.g;
          b = b + blurWith.b;
      //}
  }

  int denominator = (blurRadius * 2 + 1);
  pixelOut.r = r / denominator;
  pixelOut.g = g / denominator;
  pixelOut.b = b / denominator;
  pixelOut.a = in.a; // Preserve alpha

  return pixelOut;
}