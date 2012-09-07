
/* Plasma.java								*
 * Written by Justin Seyster and released into public domain.		*
 * Generates a "plasma fractal" using random midpoint displacement.
 * extended by Florian Rohm*/

import java.applet.Applet;
import java.awt.*;


public class Plasma extends Applet {

    //Settings *** CHANGE THIS TO GENERATE A DIFFERENT "WORLD" (mainly the seed) ***
    int seed=Integer.MAX_VALUE/10000+2; //Seed for the pseudorandom value -> Same seed, same outcome
    int width = 2048;                   //Width of one plasma tile.
                                        //Set this to height*(2^(n)), n >= 0 and integer for the function to work properly
    int height= 256/2;                  //Height of one plasma tile
    boolean posterization=false;        //set this to true if you want a posterized image at the end
    int posterizationNumber=10;         //number of colors if posterized
    float strength = 1;                 //set this number to the desired strenght level of the displacement
                                        //(0 meaning a smooth gradient, 1 default value)
    boolean nonLinFade=true;            //Set true for a nonlinear fade of the color


    boolean color=false;
    Image Buffer;	//A buffer used to store the image
    Graphics Context;	//Used to draw to the buffer.

    //Randomly displaces color value for midpoint depending on size
    //of grid piece.
    float Displace(float num, float x, float y)
    {
        float max = (num / (float)(width + height))*strength;
        return (myRandom( x,y)-0.5F) * max;
    }

     //returns value between 0 and 1 based on integer hashing
    private float myRandom(float x, float y){
        //int randInt = _noisePermutations[_noisePermutations[(int)(x*89) & 255]+(int)(y*109) & 255];
        //float randFloat= randInt/256F;

        int k=(int)x*31+(int)y*101+seed*103;
        k = (k << 13) ^ k;
        k = k * (k*k * 15731 + 789221) + 1376312589;
        return (float)((k & Integer.MAX_VALUE))/Integer.MAX_VALUE;
    }

    private double fade(double t) {
        if(nonLinFade){
            return t * t * t * (t * (t * 6 - 15) + 10);
        }
        return t;
    }

    //clips the value t to fit into [0;1]
    private static float clip(float t){
        if (t < 0)
        {
            return t = 0;
        }
        else if (t > 1.0f)
        {
            return t = 1.0f;
        }
        return t;
    }

    //Returns a color based on a color value, c.
    Color ComputeColor(float c)
    {
        float Red = 0;
        float Green = 0;
        float Blue = 0;
        if(posterization){
            c=(float)Math.floor((double)c*(posterizationNumber+1))/(posterizationNumber+1);
        }

        if (!color){
        Red=Green=Blue=(float)fade(c);
        }
        else{
            c=(float)fade(c);
            if (c < 0.5f)
            {
                Red = c * 2;
            }
            else
            {
                Red = (1.0f - c) * 2;
            }

            if (c >= 0.3f && c < 0.8f)
            {
                Green = (c - 0.3f) * 2;
            }
            else if (c < 0.3f)
            {
                Green = (0.3f - c) * 2;
            }
            else
            {
                Green = (1.3f - c) * 2;
            }

            if (c >= 0.5f)
            {
                Blue = (c - 0.5f) * 2;
            }
            else
            {
                Blue = (0.5f - c) * 2;
            }
        }
        return new Color(Red, Green, Blue);
    }

    //This is something of a "helper function" to create an initial grid
    //before the recursive function is called.
    void drawPlasma(Graphics g){
        for(int j=0; j<getSize().width; j=j+this.width){
            for(int i=0; i<getSize().height; i=i+this.height){
               drawPlasmaPiece(g, j, i);
            }
        }
    }

    //"Main" function to draw one this.width times this.height piece of noise. These get tiled to fill the screen
    void drawPlasmaPiece(Graphics g, int initialPositionX, int initialPositionY)
    {
        float c1, c2, c3, c4;

        //Assign the four corners of the initial grid random color values
        //These will end up being the colors of the four corners of the applet.

        c1 = myRandom(initialPositionX, initialPositionY);
        c2 = myRandom(initialPositionX+this.width,initialPositionY);
        c3 = myRandom(initialPositionX+this.width,initialPositionY+this.height);
        c4 = myRandom(initialPositionX,initialPositionY+this.height);

        SplitGrid(g, initialPositionX,initialPositionY, this.width , this.height , c1, c2, c3, c4);
        DivideGrid(g, initialPositionX,initialPositionY, this.width , this.height , c1, c2, c3, c4);
    }

    //Splits the grid until there are square pieces
    void SplitGrid(Graphics g, float x, float y, float width, float height, float c1, float c2, float c3, float c4){

        float Edge1, Edge2, Edge3, Edge4, Middle;
        float newWidth = width / 2;
        float newHeight = height / 2;

        if (width > 2 && height > 2)
        {
           Edge1 = (c1 + c2) / 2+ Displace(newWidth + newHeight,x+newWidth,y)/1.42F;	//Calculate the edges by averaging the two corners of each edge.
            Edge3 = (c3 + c4) / 2+ Displace(newWidth + newHeight,x+newWidth,y+height)/1.42F;

            //Make sure that the points don't accidentally "randomly displaced" past the boundaries!
            Edge1=clip(Edge1);
            Edge3=clip(Edge3);

            if(width==height){
                Middle = (c1 + c2 + c3 + c4) / 4 + Displace(newWidth + newHeight,x+newWidth,y+newWidth);	//Randomly displace the midpoint!
                Edge2 = (c3 + c4) / 2+ Displace(newWidth + newHeight,x+width,y+newHeight)/1.42F;
                Edge4 = (c3 + c4) / 2+ Displace(newWidth + newHeight,x,y+newHeight)/1.42F;

                Middle=clip(Middle);
                Edge2=clip(Edge2);
                Edge4=clip(Edge4);

                //Do the operation over again for each of the four new grids.
                DivideGrid(g, x, y, newWidth, newHeight, c1, Edge1, Middle, Edge4);
                DivideGrid(g, x + newWidth, y, newWidth, newHeight, Edge1, c2, Edge2, Middle);
                DivideGrid(g, x + newWidth, y + newHeight, newWidth, newHeight, Middle, Edge2, c3, Edge3);
                DivideGrid(g, x, y + newHeight, newWidth, newHeight, Edge4, Middle, Edge3, c4);
            }
            else{
                SplitGrid(g, x,y,newWidth,height,c1,Edge1 ,Edge3 ,c4 );
                SplitGrid(g, x+newWidth,y,newWidth, height,Edge1 ,c2 ,c3 ,Edge3 );
            }
        }
        else	//This is the "base case," where each grid piece is less than the size of a pixel.
        {
            //The four corners of the grid piece will be averaged and drawn as a single pixel.
            float c = (c1 + c2 + c3 + c4) / 4;

            g.setColor(ComputeColor(c));
            g.drawRect((int)(x), (int)(y), 1, 1);	//Java doesn't have a function to draw a single pixel, so
                                                    //a 1 by 1 rectangle is used.
        }
    }

    //This is the recursive function that implements the random midpoint
    //displacement algorithm.  It will call itself until the grid pieces
    //become smaller than one pixel.
    void DivideGrid(Graphics g, float x, float y, float width, float height, float c1, float c2, float c3, float c4)
    {
        float Edge1, Edge2, Edge3, Edge4, Middle;
        float newWidth = width / 2;
        float newHeight = height / 2;

        if (width > 2 || height > 2)
        {
            Middle = (c1 + c2 + c3 + c4) / 4 + Displace(newWidth + newHeight,x+newWidth,y+newWidth);	//Randomly displace the midpoint!
            Edge1 = (c1 + c2) / 2+ Displace(newWidth + newHeight,x+newWidth,y)/1.42F;	//Calculate the edges by averaging the two corners of each edge.
            Edge2 = (c2 + c3) / 2+ Displace(newWidth + newHeight,x+width,y+newHeight)/1.42F;
            Edge3 = (c3 + c4) / 2+ Displace(newWidth + newHeight,x+newWidth,y+height)/1.42F;
            Edge4 = (c4 + c1) / 2+ Displace(newWidth + newHeight,x,y+newHeight)/1.42F;

            //Make sure that the points don't accidentally "randomly displaced" past the boundaries!
            Middle=clip(Middle);
            Edge1=clip(Edge1);
            Edge2=clip(Edge2);
            Edge3=clip(Edge3);
            Edge4=clip(Edge4);

            //Do the operation over again for each of the four new grids.
            DivideGrid(g, x, y, newWidth, newHeight, c1, Edge1, Middle, Edge4);
            DivideGrid(g, x + newWidth, y, newWidth, newHeight, Edge1, c2, Edge2, Middle);
            DivideGrid(g, x + newWidth, y + newHeight, newWidth, newHeight, Middle, Edge2, c3, Edge3);
            DivideGrid(g, x, y + newHeight, newWidth, newHeight, Edge4, Middle, Edge3, c4);
        }
        else	//This is the "base case," where each grid piece is less than the size of a pixel.
        {
            //The four corners of the grid piece will be averaged and drawn as a single pixel.
            float c = (c1 + c2 + c3 + c4) / 4;

            g.setColor(ComputeColor(c));
            g.drawRect((int)(x), (int)(y), 1, 1);	//Java doesn't have a function to draw a single pixel, so
            //a 1 by 1 rectangle is used.
        }
    }

    //Chance the color value on click (invert boolean color and repaint)
    public boolean mouseUp(Event evt, int x, int y){
        color=!color;
        drawPlasma(Context);
        repaint();	//Force the applet to draw the new plasma fractal.
        return false;
    }

    //Whenever something temporarily obscures the applet, it must be redrawn manually.
    //Since the fractal is stored in an offscreen buffer, this function only needs to
    //draw the buffer to the screen again.
    public void paint(Graphics g)
    {
        g.drawImage(Buffer, 0, 0, this);
    }

    public String getAppletInfo()
    {
        return "Extended Plasma Fractal. Written September 2012, by Florian Rohm. Initial implementation January, 2002 by Justin Seyster.";
    }

    public void init()
    {
        Buffer = createImage(getSize().width, getSize().height);	//Set up the graphics buffer and context.
        Context = Buffer.getGraphics();
        drawPlasma(Context);	//Draw the first plasma fractal.
    }
};
