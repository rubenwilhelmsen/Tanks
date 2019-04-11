import java.util.ArrayList;

public class TankNode {
    private int row , col;
    private ArrayList<TankNode> children;
    private TankNode parent;
    //-----------------------------------Constructors----------------------------------------------
    public TankNode(int row, int col, TankNode parent){
        this.row = row;
        this.col = col;
        this.parent = parent;
    }
    //-----------------------------------Methods---------------------------------------------------

    public  void addChild(int row, int col){
        children.add(new TankNode(row,col, this));
    }



}
