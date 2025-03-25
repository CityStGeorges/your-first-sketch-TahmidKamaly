
// -- simple sketch to show a number between 1 and 52

void setup () {               // <-- runs once 
  
  size(100,100);              // -- we only need a small space for a single number
  frameRate(1);               // -- draw loop runs once per second
  textAlign(CENTER,CENTER);   // -- text will be centred on the coordinates that we give to text()
  textSize(32);               // -- text will be 32 pixels high
}

void draw () {                // <-- runs continuously (speed set by frameRate variable)

  background(250);            // -- clear the background with a lightish grey
  fill(235);                  // -- set the fill colour to something a little darker for the rectangle
  rect(20,20,60,60);          // -- draw a rectangle in the centre of the canvas  

  float rF = random(0,52);    // -- create a float between 0 and 52 and store this in rF
  int   rI = ceil(rF);        // -- create an integer from rF and store this in rI

  fill(96);                   // -- set the fill colour to something darkish for text
  text(rI,50,50);             // -- add some text at 50,50 containing the value of 'rF'
}
