///<reference path="./../interfaces.d.ts" />

class MathDemo implements MathInterface{
  public PI : number;

  constructor() {
    this.PI = 3.14159265359;
  }

  // used to showcase how to test a sync function
  public pow(base: number, exponent: number) {
    var result = base;
    for(var i = 1; i < exponent; i++){
      result = result * base;
    }
    return result;
  }

  // used to show case how to test async code with done()
  public powAsync(base: number, exponent: number, cb : (result : number) => void) {
    var result = this.pow(base, exponent);
    cb(result);
  }

  // simulate slow > 40ms
  public powAsyncSlow(base: number, exponent: number, cb : (result : number) => void) {
    setTimeout(() => {
      var result = this.pow(base, exponent);
      cb(result);
    }, 45);
  }

  // simulate reelly slow function > 100ms
  public powAsyncReallySlow(base: number, exponent: number, cb : (result : number) => void) {
    var result = base ^ exponent;
    setTimeout(() => {
      var result = this.pow(base, exponent);
      cb(result);
    }, 101);
  }

  // simulate too slow > 2000ms (breaks build)
  public powAsyncTooSlow(base: number, exponent: number, cb : (result : number) => void) {
    var result = base ^ exponent;
    setTimeout(() => {
      var result = this.pow(base, exponent);
      cb(result);
    }, 2001);
  }

  // used to showcase how to assert that an error is thrown
  public bad(foo : any) {
    if(foo == null){
      throw new Error("Error!");
    }
    else {
      //...
    }
  }


}

export { MathDemo };
