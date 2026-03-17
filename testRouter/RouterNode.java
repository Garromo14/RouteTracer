
public class RouterNode implements Runnable{
  private int myID;
  private GuiTextArea myGUI;
  private RouterSimulator sim;
  private int[] costs;
  private boolean[] nbrs;
  private int[][] dist;
  private int dest;
  private int [] route;

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
    this.myID = ID;
    this.sim = sim;
    this.costs = new int[sim.NUM_NODES];
    this.nbrs = new boolean[sim.NUM_NODES];
    this.dist = new int[sim.NUM_NODES][sim.NUM_NODES];
    this.route = new int[sim.NUM_NODES];

    myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
    
    System.arraycopy(costs, 0, this.costs, 0, sim.NUM_NODES);

    //Initializing: neighbors(if there is a direct link to node i = true) and route(The default route is to go directly)
    for(int i = 0; i < this.costs.length; i++){
        if((this.costs[i] != RouterSimulator.INFINITY) && (i != this.myID)){
            this.nbrs[i] = true;
            this.route[i] = i;
        }
        if(i == this.myID){
            this.nbrs[i] = false;
            this.route[i] = i;
        }
    }

    //Initialize the distance table
    for (int i = 0; i < this.costs.length; i++) {
        for(int j = 0; j < this.costs.length; j++){
            if(i == myID){
                this.dist[i][j] = this.costs[j];
            }
            else if(i == j){
                this.dist[i][j] = 0;
            }
            else{
                this.dist[i][j] = RouterSimulator.INFINITY;
            }
        }
    }

    //Send a new update to your neighbors
    for(int i = 0; i < this.costs.length; i++){
        if(this.nbrs[i]){
            this.dest = i;
            new Thread(this).run();  
        }
        
    }
  }

  //--------------------------------------------------
  public void recvUpdate(RouterPacket pkt){
    int source = pkt.sourceid;
    int[] mincost = pkt.mincost;
    boolean updated = false;    

    //update the dist[][] for all the values that go through source
    for(int i = 0; i < this.costs.length; i++){
            //dist from source to every node
            this.dist[source][i] = mincost[i]; 
            
            //dist from myID to every node when the next hop is source
            if(route[i] == source){
                if(this.dist[myID][i] < (mincost[i] + this.costs[source])){
                    this.dist[myID][i] = mincost[i] + this.costs[source];
                    updated = true;
                }
            }
    }

    //Bellman-Ford
    for(int i = 0; i < this.costs.length; i++){
        int min = 999;
        int minroute = 999;

        //Search for the minimum route
        for(int j = 0; j < this.costs.length; j++){
            if((this.costs[j] + this.dist[j][i]) < min){
                min = (this.costs[j] + this.dist[j][i]);
                minroute = j;
            }
        }
            
        //If the min route is smaller than the one I had stored, change it
        if(min < this.dist[myID][i]){
            this.dist[myID][i] = min;
            this.route[i] = minroute;
            updated = true;
        }
    }  

    
    //Apply poisoned reverse
    if(RouterSimulator.POISONREVERSE){
        int [] recover = new int[this.costs.length];

        //If the route to go to a node goes through source make the distance from myself to that node INF
        for(int i = 0; i < this.costs.length; i++){
            recover[i] = this.dist[myID][i];
                if((this.route[i] == source)){
                    this.dist[myID][i] = RouterSimulator.INFINITY;
                }
        }

        //Send update to source
        this.dest = source;
        if(updated)
            new Thread(this).run();


        //Restore the correct distance values
        this.dist[myID] = recover;
    }

    //Send update
    if(!RouterSimulator.POISONREVERSE){ //Default
        if(updated){
            for(int i = 0; i < this.costs.length; i++){
                if(nbrs[i]){
                    this.dest = i;
                    new Thread(this).run();
                }
            }
        }
    }
    else{  //In the case of poison reverse don't send to source
        if(updated){
            for(int i = 0; i < this.costs.length; i++){
                if(nbrs[i] && (i != source)){
                    this.dest = i;
                    new Thread(this).run();
                }
            }
        }
    }
    
  }

  //--------------------------------------------------
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);

  }  


  // Updates the cost to go to dest with newcost and then uses Bellman-Ford with the updated value
  // to look for the new minimum cost to go to every node
  //--------------------------------------------------
  public void updateLinkCost(int dest, int newcost) {

    //Update the cost with new value
    this.costs[dest] = newcost;
    this.dist[myID][dest] = newcost;
    this.route[dest] = dest;

    //Bellman-Ford
    for(int i = 0; i < this.costs.length; i++){
        int min = 999;
        int minroute = 999;

        //Search for the minimum route
        for(int j = 0; j < this.costs.length; j++){
            if((this.costs[j] + this.dist[j][i]) < min){
                min = (this.costs[j] + this.dist[j][i]);
                minroute = j;
            }
        }
            
        //If the min route is smaller than the one I had stored, change it
        if(min < this.dist[myID][i]){
            this.dist[myID][i] = min;
            this.route[i] = minroute;
        }
    }

    //Send update to neighbors
    for(int i = 0; i < this.costs.length; i++){
        if(this.nbrs[i]){
            this.dest = i;
            new Thread(this).run();  
        }
    }
  }

    //--------------------------------------------------
    public void printDistanceTable() {
            myGUI.println("Current table for " + myID +
                          "  at time " + sim.getClocktime());

            myGUI.println();

            /*********************  PRINTS THE MINIMUM DISTANCE TO GO FROM A CERTAIN    *******************/
            /*********************            NODE TO EVERY OTHER NODE                  *******************/

            myGUI.println("DistanceTable:");

            myGUI.println();

            myGUI.print(F.format("dst", 10) + F.format("|", 6));
            for(int i = 0; i < this.costs.length; i++){
                myGUI.print(F.format(i, 20));
            }
            myGUI.println();

            for(int i = 0; i < 25*this.costs.length; i++){
                myGUI.print("-");
            }
            myGUI.println();

            for(int j = 0; j < this.costs.length; j++){
              if((this.costs[j] != RouterSimulator.INFINITY) && (j != this.myID)){
                  myGUI.print(F.format("nbr", 5) + F.format(j, 5) + F.format("|", 5));
                  for(int i = 0; i < this.costs.length; i++){
                      myGUI.print(F.format(dist[j][i], 20));
                  }
                  myGUI.println();
              }
            }
            myGUI.println();
            myGUI.println();



            /*******************    PRINTS THE MINIMUM DISTANCE TO GO FROM MYSELF    **********************/
            /*******************                  TO ANY OTHER NODE                  **********************/

            myGUI.println("Distance vector and routes:");

            myGUI.println();

            myGUI.print(F.format("dst", 10) + F.format("|", 6));
            for(int i = 0; i < this.costs.length; i++){
                myGUI.print(F.format(i, 20));
            }
            myGUI.println();

            for(int i = 0; i < 25*this.costs.length; i++){
                myGUI.print("-");
            }
            myGUI.println();

            myGUI.print(F.format("cost", 10) + F.format("|", 5));
            for(int i = 0; i < this.costs.length; i++){
                myGUI.print(F.format(this.dist[myID][i], 20));
            }
            myGUI.println();

            myGUI.print(F.format("route", 10) + F.format("|", 4));
            for(int i = 0; i < this.costs.length; i++){
                myGUI.print(F.format(this.route[i], 20));
            }
            myGUI.println();
  }

    //Use threads to send the update asynchronously
    @Override
    public void run() {
        sendUpdate(new RouterPacket(myID, this.dest, this.dist[myID]));
    }

}
