// Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package org.gtagency.autotetris.field;

import java.awt.Color;
import java.awt.Point;

import org.gtagency.autotetris.field.Cell;
import org.gtagency.autotetris.field.Field;
import org.gtagency.autotetris.field.ShapeType;
import org.gtagency.autotetris.moves.MoveType;

/**
 * Shape class
 * 
 * Represents the shapes that appear in the field.
 * Some basic methods have already been implemented, but
 * actual move actions, etc. should still be created.
 * 
 * @author Jim van Eeden <jim@starapple.nl>
 */

public class Shape {

    public ShapeType type;
    private int orientation;
    private Cell[][] shape; // 2-dimensional bounding box: a matrix that contains the block-cells of the shape
    private Cell[] blocks; // array that contains only the block-cells of the shape
    private int size;
    private Point location;

    public Shape(ShapeType type, Point location) {
        this.type = type;
        this.blocks = new Cell[4];
        this.location = location;

        setShape();
        setBlockLocations();
    }

    public boolean isAt(Point p) {
        for (Cell cell : blocks) {
            if (cell.getLocation().equals(p)) {
                return true;
            }
        }
        return false;
    }

    public void place(Field f){
        for (Cell cell : blocks) {
            f.getCell(cell.getLocation().x, cell.getLocation().y).setColor(type.color());
            f.getCell(cell.getLocation().x, cell.getLocation().y).setState(CellType.BLOCK);
        }
    }

    public void unplace(Field f){ //reverses the place method, should NOT be called otherwise
        for (Cell cell : blocks) {
            f.getCell(cell.getLocation().x, cell.getLocation().y).setColor(Color.BLUE);
            f.getCell(cell.getLocation().x, cell.getLocation().y).setState(CellType.EMPTY);
        }
    }

    public boolean hasCollision(Field f) {
        for (Cell cell : blocks) {
            if (cell.hasCollision(f)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOutOfBoundaries(Field f) {
        for (Cell cell : blocks) {
            if (cell.isOutOfBoundaries(f)) {
                return true;
            }
        }
        return false;
    }

    // ACTIONS (no checks for errors are performed in the actions!)

    /**
     * Rotates the shape counter-clockwise
     */
    public void turnLeft() {
        orientation=(orientation+3)%4;
        Cell[][] temp = this.transposeShape();
        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                this.shape[x][y] = temp[x][size - y - 1];
            }
        }

        this.setBlockLocations();
    }

    /**
     * Rotates the shape clockwise
     */
    public void turnRight() {
        orientation=(orientation+1)%4;
        Cell[][] temp = this.transposeShape();
        for(int x=0; x < size; x++) {
            this.shape[x] = temp[size - x - 1];
        }

        this.setBlockLocations();
    }

    public void oneDown() {

        this.location.y++;
        this.setBlockLocations();
    }

    public void oneUp() {

        this.location.y--;
        this.setBlockLocations();
    }

    public void oneRight() {

        this.location.x++;
        this.setBlockLocations();
    }

    public void oneLeft() {

        this.location.x--;
        this.setBlockLocations();
    }

    /**
     * Used for rotations
     * @return transposed matrix of current shape box
     */
    private Cell[][] transposeShape() {
        Cell[][] temp = new Cell[size][size];
        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                temp[y][x] = shape[x][y];
            }
        }
        return temp;
    }

    /**
     * Uses the shape's current orientation and position to
     * set the actual location of the block-type cells on the field
     */
    private void setBlockLocations() {
        for(int y=0; y < size; y++) {
            for(int x=0; x < size; x++) {
                if(shape[x][y].isShape()) {
                    shape[x][y].setLocation(location.x + x, location.y + y);
                }
            }
        }
    }

    /** 
     * Set shape in square box.
     * Creates new Cells that can be checked against the actual
     * playing field.
     * */
    private void setShape() {
        switch(this.type) {
        case I:
            this.size = 4;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[0][1];
            this.blocks[1] = this.shape[1][1];
            this.blocks[2] = this.shape[2][1];
            this.blocks[3] = this.shape[3][1];
            break;
        case J:
            this.size = 3;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[0][0];
            this.blocks[1] = this.shape[0][1];
            this.blocks[2] = this.shape[1][1];
            this.blocks[3] = this.shape[2][1];
            break;
        case L:
            this.size = 3;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[2][0];
            this.blocks[1] = this.shape[0][1];
            this.blocks[2] = this.shape[1][1];
            this.blocks[3] = this.shape[2][1];
            break;
        case O:
            this.size = 2;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[0][0];
            this.blocks[1] = this.shape[1][0];
            this.blocks[2] = this.shape[0][1];
            this.blocks[3] = this.shape[1][1];
            break;
        case S:
            this.size = 3;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[1][0];
            this.blocks[1] = this.shape[2][0];
            this.blocks[2] = this.shape[0][1];
            this.blocks[3] = this.shape[1][1];
            break;
        case T:
            this.size = 3;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[1][0];
            this.blocks[1] = this.shape[0][1];
            this.blocks[2] = this.shape[1][1];
            this.blocks[3] = this.shape[2][1];
            break;
        case Z:
            this.size = 3;
            this.shape = initializeShape();
            this.blocks[0] = this.shape[0][0];
            this.blocks[1] = this.shape[1][0];
            this.blocks[2] = this.shape[1][1];
            this.blocks[3] = this.shape[2][1];
            break;
        }

        // set type to SHAPE
        for(int i=0; i < blocks.length; i++) {
            this.blocks[i].setShape();
        }
    }

    /**
     * Creates the matrix for the shape
     * @return
     */
    private Cell[][] initializeShape() {
        Cell[][] newShape = new Cell[size][size];
        for(int y=0; y < this.size; y++) {
            for(int x=0; x < this.size; x++) {
                newShape[x][y] = new Cell();
            }
        }
        return newShape;
    }

    public boolean checkTSpin(Field field, MoveType lastMove1, MoveType lastMove2, Point lastLocation) {
        if(this.type != ShapeType.T)
            return false;

        if(lastMove1 == null || lastMove2 == null)
            return false;

        // last move is turn or second to last move is turn
        if(!(lastMove1 == MoveType.TURNRIGHT || lastMove1 == MoveType.TURNLEFT 
                || ((lastMove1 == MoveType.DOWN || lastMove1 == MoveType.DROP)
                        && (lastMove2 == MoveType.TURNLEFT || lastMove2 == MoveType.TURNRIGHT)
                        && (lastLocation.equals(this.location)))))
            return false;

        // check if 3/4 corners of the matrix are Blocks in the field
        Cell[] corners = new Cell[4];
        corners[0] = field.getCell(this.location.x, this.location.y);
        corners[1] = field.getCell(this.location.x + 2, this.location.y);
        corners[2] = field.getCell(this.location.x, this.location.y + 2);
        corners[3] = field.getCell(this.location.x, this.location.y + 2);

        int counter = 0;
        for(int i = 0; i < corners.length; i++)
            if(corners[i] != null && corners[i].isBlock())
                counter++;

        if(counter == 3)
            return true;

        return false;
    }


    public void setLocation(int x, int y) {
        this.location = new Point(x, y);
        setBlockLocations();
    }

    public void setLocation(Point p) {
        setLocation(p.x, p.y);
    }

    public void setDefaultLocation() {
        setLocation(type.startPos());
    }

    public Cell[] getBlocks() {
        return this.blocks;
    }

    public Point getLocation() {
        return this.location;
    }

    public int getOrientation(){
        return orientation;
    }

    public ShapeType getType() {
        return this.type;
    }

    public int getSize(){
        return size;
    }
}
