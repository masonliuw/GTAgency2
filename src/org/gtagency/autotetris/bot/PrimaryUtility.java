package org.gtagency.autotetris.bot;

import java.util.PriorityQueue;

import org.gtagency.autotetris.field.Field;
import org.gtagency.autotetris.bot.BotStarter.Node;
import org.gtagency.autotetris.field.Cell;

public class PrimaryUtility implements Utility {//TODO GA, field height changes?, change clear desirabilty?, cell-by-cell tspin check, contour

    public double value(Field field, Node firstMove, Node secondMove, BotState state, int par) {
        /////////////////////////////////////
        //cell neighbor utilities

        Cell[] neighbors = new Cell[4];
        int h1 = 0;
        int totalBlocks = 0;
        double holes = 0;
        double colWeight = 0;
        int maxHeight = field.getHeight();
        int[] localMaxHeights = new int[field.getWidth()];

        for(int i=0; i<field.getWidth(); i++){
            int localMaxHeight = field.getHeight();
            for (int j=0; j<field.getHeight(); j++){
                Cell c = field.getCell(i,j);
                if(!c.isEmpty()){
                    if(c.isBlock()){
                        totalBlocks++;
                        colWeight += Math.pow((field.getWidth()/2 - Math.abs(field.getWidth()/2 - i)),2)/2;
                    }
                    // Higher is closer to 0
                    if (j < localMaxHeight) {
                        localMaxHeight = j;
                    }

                    neighbors[0]=field.getCell(i-1,j);
                    neighbors[1]=field.getCell(i+1,j);
                    neighbors[2]=field.getCell(i,j+1);
                    neighbors[3]=field.getCell(i,j-1);

                    for (Cell n: neighbors){
                        if (n!= null && n.isEmpty()) {
                            h1++; //penalty for high arclen
                        }
                    }
                } else {
                    Cell n;
                    Cell l;
                    Cell r;
                    int k = 0;

                    if (j > localMaxHeight){
                        l = field.getCell(i-1, j);
                        r = field.getCell(i+1, j);
                        if((l == null || !l.isEmpty()) && (r == null || !r.isEmpty())){
                            h1+=8; //extra penalty if surrounded on sides
                            holes += .5;
                        }
                        h1+= 10; //for multiple empty blocks in same col
                        holes += .5;
                    }

                    do{
                        k++;
                        n = field.getCell(i, j-k);
                        if(n != null && (!n.isEmpty())){
                            h1 += 20 * Math.pow(.75,k-1); //for multiple filled blocks over empty block
                        }
                    }while(n!= null && (!n.isEmpty()));
                }
            }
            localMaxHeights[i] = field.getHeight() - localMaxHeight;
            if(localMaxHeight < maxHeight){
                maxHeight = localMaxHeight;
            }
        }

        ////////////////////////////////////////////
        //height utilities
        int h2 = 0;
        maxHeight = field.getHeight() - maxHeight;
        field.setBlockHeight(maxHeight);

        //penalizes extreme heights
        h2 += 200*(Math.pow(((double)Math.max((maxHeight - field.getHeight()/2),0)),2)/Math.pow((double)field.getHeight()/2,2));
        h2 += 120*(((double)Math.max((maxHeight - field.getHeight()/2),0))/((double)field.getHeight()/2));

        if(totalBlocks == 0){
            h2 -= 700; //perfect clear
        } else {
            //h1 -= totalBlocks; //counterbalances row clear bonuses
            h2 += 8 * colWeight/totalBlocks; //encourages upstacking on sides
        }

        double forgivenCol = 0;
        for(int i=1; i<localMaxHeights.length; i++){
            double penalty = 2*Math.pow(Math.max(Math.abs(localMaxHeights[i]-localMaxHeights[i-1])-1, 0),2);
            h2 += penalty; //penalizes column height differences
            //h2 += Math.max(Math.abs(localMaxHeights[i]-localMaxHeights[i-1] - ((i<field.getWidth()/2)? -1 : 1)), 0)/2; //convex heuristic
            if(penalty > forgivenCol){
                forgivenCol = penalty;
            }
        }
        h2 -= forgivenCol; //allows 1 large height difference

        forgivenCol = 0;
        for(int i=0; i<localMaxHeights.length/2; i++){
            double penalty = Math.pow(Math.max(Math.abs(localMaxHeights[i]-localMaxHeights[field.getWidth() - 1 - i])-1, 0),2)/3;
            h2 += penalty; //penalizes asymmetry
            if(penalty > forgivenCol){
                forgivenCol = penalty;
            }
        }
        h2 -= forgivenCol; //allows 1 large symmetric difference

        PriorityQueue<Integer> forgiven = new PriorityQueue<>();
        forgiven.add(0);
        forgiven.add(0);
        for(int i=0; i<localMaxHeights.length; i++){
            double penalty = Math.max(maxHeight-localMaxHeights[i] - 5, 0);
            h2 += penalty; //penalizes max-min height differences
            if(penalty > forgiven.peek()){
                forgiven.poll();
                forgiven.add((int) penalty);
            }
        }
        h2-=forgiven.poll(); //allows 2 max-min height differences
        h2-=forgiven.poll();


        /////////////////////////////
        //rows cleared utilities
        int h3 = 200;
        //-1 is highly desirable, 1 is highly undesirable
        double clearDesirability =  ((maxHeight > field.getHeight()/2)?-1:1) * Math.pow(((double)(maxHeight - field.getHeight()/2)),2)/Math.pow((double)field.getHeight()/2,2);
        //-1 is highly desirable, 0 is highly undesirable
        double downstackDesirability = (-Math.min(1,Math.pow(holes/5,3)));
        int firstCleared  = (firstMove == null)? 0 : firstMove.cleared;
        int secondCleared = (secondMove == null)? 0 : secondMove.cleared;

        int oneCleared = ((firstCleared == 1)?1:0) + ((secondCleared == 1)?1:0);
        h3 += 24 * oneCleared * (clearDesirability + downstackDesirability + .6);

        int twoCleared = ((firstCleared == 2)?1:0) + ((secondCleared == 2)?1:0);
        h3 += 24 * twoCleared * (clearDesirability + downstackDesirability);

        int threeCleared = ((firstCleared == 3)?1:0) + ((secondCleared == 3)?1:0);
        h3 += 35 * threeCleared * (clearDesirability + downstackDesirability -.6);

        int fourCleared = ((firstCleared == 4)?1:0) + ((secondCleared == 4)?1:0);
        h3 -= 65 * (1 - downstackDesirability) * fourCleared;
        /////////////////////////////
        //tspin utilities
        int h4 = 400;
        int firstSpin = (firstMove != null && firstMove.tspin)? firstCleared : 0;
        int secondSpin = (secondMove != null && secondMove.tspin)? secondCleared : 0;

        int oneSpin = ((firstSpin == 1)?1:0) + ((secondSpin == 1)?1:0);
        h4 -= 60* Math.pow(oneSpin,2);

        int twoSpin = ((firstSpin == 2)?1:0) + ((secondSpin == 2)?1:0);
        h4 -= 90* Math.pow(twoSpin,2);

        /////////////////////////////
        return 2*h1 + h2 + h3 + h4;

    }
}