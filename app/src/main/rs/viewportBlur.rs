#pragma version(1)
#pragma rs java_package_name(com.mystokely.rsplayground)

rs_allocation inAllocation;
int32_t yApplyUntil = 0;
int32_t yApplyAfter = 10000;
short blurRadius = 20;

uchar4 RS_KERNEL blur(uint32_t x, uint32_t y){
  uchar4 in = rsGetElementAt_uchar4(inAllocation, x, y);
  if(y >= yApplyUntil && y <= yApplyAfter){
    return in;
  }

  uint32_t xMax = rsAllocationGetDimX(inAllocation);
  uint32_t yMax = rsAllocationGetDimY(inAllocation);

  uint4 sum = 0;
  uint count = 0;
  for (int yi = -blurRadius; yi <= blurRadius; ++yi) {
    if(yi < 0){
      continue;
    }
    sum += convert_uint4(rsGetElementAt_uchar4(inAllocation, x, y+yi));
    ++count;
  }

  for (int xi = -blurRadius; xi <= blurRadius; ++xi) {
    if(xi < 0){
      continue;
    }
    sum += convert_uint4(rsGetElementAt_uchar4(inAllocation, x+xi, y));
    ++count;
  }

  return convert_uchar4(sum/count);
}