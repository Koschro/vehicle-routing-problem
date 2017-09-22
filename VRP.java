
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;



public class VRP {


	public static void main(String[] args) 
	{
		Random ran = new Random(180691);

		//Set up Input for TSP
		final int numberOfCustomers  = 30;
		//max vehicles
		final int numberOfVehicles = 10;

		//Create the depot
		Node depot = new Node();
		depot.x = 50;
		depot.y = 50;

		//Create the list with the customers
		ArrayList <Node> customers = new ArrayList<Node>();
		for (int i = 0 ; i < numberOfCustomers; i++)
		{
			Node cust = new Node();

			cust.x = ran.nextInt(100);
			cust.y = ran.nextInt(100);
		//demand gia ka8e pelath dinontai ta upoloipa.
			cust.demand = 4 + ran.nextInt(7);
			cust.ID = i;
			customers.add(cust);
		}


		//Build the allNodes array and the corresponding distance matrix
		ArrayList <Node> allNodes = new ArrayList<Node>();


		allNodes.add(depot);
		for (int i = 0 ; i < customers.size(); i++)
		{
			Node cust = customers.get(i);
			allNodes.add(cust);
		}

		for (int i = 0 ; i < allNodes.size(); i++)
		{
			Node nd = allNodes.get(i);
			nd.ID = i;
		}

        double [][] distanceMatrix = new double [allNodes.size()][allNodes.size()];
		for (int i = 0 ; i < allNodes.size(); i++)
		{
			Node from = allNodes.get(i);

			for (int j = 0 ; j < allNodes.size(); j++)
			{
				Node to = allNodes.get(j);

				double Delta_x = (from.x - to.x);
				double Delta_y = (from.y - to.y);
				double distance = Math.sqrt((Delta_x * Delta_x) + (Delta_y * Delta_y));

				distance = Math.round(distance);

				distanceMatrix[i][j] = distance;

			}
		}


		Solution s = new Solution();

		Route route = s.rt;
		
		ArrayList <Route> setOfRoutes = s.sr;
                ArrayList <Node> nodeSequence = route.nodes;

		nodeSequence.add(depot);
				
				//ITERATIVE BODY OF THE NN ALGORITHM
                                
				for (int i = 0 ; i < customers.size(); i++)
				{
					//this will be the position of the nearest neighbor customer -- initialization to -1
					int positionOfTheNextOne = -1;

					// This will hold the minimal cost for moving to the next customer - initialized to something very large
					double bestCostForTheNextOne = Double.MAX_VALUE;

					//This is the last customer of the route (or the depot if the route is empty)
					Node lastInTheRoute = nodeSequence.get(nodeSequence.size() - 1);
					
					
					//First Step: Identify the non-routed nearest neighbor (his position in the customers list) of the last node in the nodeSequence list
					for (int j = 0 ; j < customers.size(); j++)
					{
						// The examined node is called candidate
						Node candidate = customers.get(j);

						// if this candidate has not been pushed in the solution and the vehicle can take the demand of the customer
						if (candidate.isRouted == false && route.load + candidate.demand <= route.capacity )
						{
							//This is the cost for moving from the last to the candidate one
							double trialCost = distanceMatrix[lastInTheRoute.ID][candidate.ID];

							//If this is the minimal cost found so far -> store this cost and the position of this best candidate
							if (trialCost < bestCostForTheNextOne)
							{
								positionOfTheNextOne = j;
								bestCostForTheNextOne = trialCost;
							}
						}
					}
						//to avoid the first iteration problem
                        if(positionOfTheNextOne != -1 ){						
			
			Node insertedNode = customers.get(positionOfTheNextOne);
                        
			nodeSequence.add(insertedNode);
                        
			double testCost = distanceMatrix[lastInTheRoute.ID][insertedNode.ID];

			if (testCost != bestCostForTheNextOne)
			{
				//If we are not correct
				System.out.println("Something has gone wrong with the cost calculations !!!!");
			}
			//the route cost is updated with the new cost by adding it to the sum of previous costs
						route.cost += bestCostForTheNextOne;
                        route.load += customers.get(positionOfTheNextOne).demand;                      
                        
                        insertedNode.isRouted = true;
			
                        s.rt = route;
                        s.cost = route.cost;
                        drawRoutes(s, allNodes,"Vehicle" + (setOfRoutes.size()+1) + "_" + (nodeSequence.size()-1));
			
                        //this if allows us to save the last route
                        if ( i == customers.size() - 1 )
                           {                            
                            s.cost =  route.cost + distanceMatrix[lastInTheRoute.ID][depot.ID];
                            nodeSequence.add(depot);
                            drawRoutes(s, allNodes,"Vehicle" + (setOfRoutes.size()+1) + "_" + (nodeSequence.size()-1));
                            setOfRoutes.add(route);                            
                            }
                        }
                        
                        else
                           {
                        		//for a total allowance of 10 vehicles
                               if( setOfRoutes.size() < numberOfVehicles )
                               {
                                   nodeSequence.add(depot);
                                   s.rt = route;
                                   s.cost =  route.cost + distanceMatrix[lastInTheRoute.ID][depot.ID];
                                   drawRoutes(s, allNodes,"Vehicle" +(setOfRoutes.size()+1) + "_" + (nodeSequence.size()-1));
                                   setOfRoutes.add(route);      
                                   //A new vehicle is enabled
                                   route = new Route();
                                   nodeSequence = route.nodes;
                                   nodeSequence.add(depot);
                                   //this i-- is for the the case that a vehicle cannot fit any remaining customers,
                                   //but these customers should be examined by other vehicles
                                   i--;
                                }
                               else 
                               {
                            	   //there are not any more vehicles so finalize solution by adding the depot
                                   nodeSequence.add(depot);
                                   s.rt = route;
                                   s.cost =  route.cost + distanceMatrix[lastInTheRoute.ID][depot.ID];
                                   drawRoutes(s, allNodes,"Vehicle" +(setOfRoutes.size()+1) + "_" + (nodeSequence.size()-1));
                                   setOfRoutes.add(route); 
                                   System.out.println("There are no more vehicles !!!");
                                   break;
                               }
                            }
			
                                }
			
				//This loop allow us to print the routes,each associated cost and the overall cost.
				int totalCost = 0;
                for ( int i = 0; i < setOfRoutes.size(); i++ )
                {
                    totalCost += setOfRoutes.get(i).cost;
                    
                    System.out.println("Cost of vehicle_" + ( i +1 ) + " :  " + setOfRoutes.get(i).cost);
                    System.out.println("Load of vehicle_" + ( i+1 ) + " :  " + setOfRoutes.get(i).load);
                    System.out.print("Sequence Of Customers: ");
                    
                    for ( int j = 0; j < setOfRoutes.get(i).nodes.size(); j++)
                    {
                        System.out.print(setOfRoutes.get(i).nodes.get(j).ID + "  ");
                    }
                    System.out.println("\n");
                }

                System.out.println("Total Cost: " + totalCost);                   
	}

	private static void drawRoutes(Solution s, ArrayList<Node> nodes, String fileName) 
	{

		int VRP_Y = 800;
		int VRP_INFO = 200;
		int X_GAP = 600;
		int margin = 30;
		int marginNode = 1;
		int XXX =  VRP_INFO + X_GAP;
		int YYY =  VRP_Y;


		BufferedImage output = new BufferedImage(XXX, YYY, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = output.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, XXX, YYY);
		g.setColor(Color.BLACK);


		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (int i = 0; i < nodes.size(); i++)
		{
			Node n = nodes.get(i);
			if (n.x > maxX) maxX = n.x;
			if (n.x < minX) minX = n.x;
			if (n.y > maxY) maxY = n.y;
			if (n.y < minY) minY = n.y;
		}

		int mX = XXX - 2 * margin;
		int mY = VRP_Y - 2 * margin;

		int A, B;
		if ((maxX - minX) > (maxY - minY))
		{
			A = mX;
			B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
			if (B > mY)
			{
				B = mY;
				A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
			}
		}
		else
		{
			B = mY;
			A = (int)((double)(B) * (maxX - minX) / (maxY - minY));
			if (A > mX)
			{
				A = mX;
				B = (int)((double)(A) * (maxY - minY) / (maxX - minX));
			}
		}

		// Draw Route
		for (int i = 1; i < s.rt.nodes.size(); i++)
		{
			Node n;
			n = s.rt.nodes.get(i - 1);
			int ii1 = (int)((double)(A) * ((n.x - minX) / (maxX - minX) - 0.5) + (double)mX / 2) + margin;
			int jj1 = (int)((double)(B) * (0.5 - (n.y - minY) / (maxY - minY)) + (double)mY / 2) + margin;
			n = s.rt.nodes.get(i);
			int ii2 = (int)((double)(A) * ((n.x - minX) / (maxX - minX) - 0.5) + (double)mX / 2) + margin;
			int jj2 = (int)((double)(B) * (0.5 - (n.y - minY) / (maxY - minY)) + (double)mY / 2) + margin;


			g.drawLine(ii1, jj1, ii2, jj2);
		}

		for (int i = 0; i < nodes.size(); i++)
		{
			Node n = nodes.get(i);

			int ii = (int)((double)(A) * ((n.x - minX) / (maxX - minX) - 0.5) + (double)mX / 2) + margin;
			int jj = (int)((double)(B) * (0.5 - (n.y - minY) / (maxY - minY)) + (double)mY / 2) + margin;
			if (i != 0)
			{
				g.fillOval(ii - 2 * marginNode, jj - 2 * marginNode, 4 * marginNode, 4 * marginNode);
				String id = Integer.toString(n.ID);
				g.drawString(id, ii + 8 * marginNode, jj+ 8 * marginNode);
			}
			else
			{
				g.fillRect(ii - 4 * marginNode, jj - 4 * marginNode, 8 * marginNode, 8 * marginNode);
				String id = Integer.toString(n.ID);
				g.drawString(id, ii + 8 * marginNode, jj + 8 * marginNode);
			}
		}

		String cst = "Cost: " + s.cost;
		g.drawString(cst, 10, 10);

		fileName = fileName + ".png";
		File f = new File(fileName);
		try 
		{
			ImageIO.write(output, "PNG", f);
		} catch (IOException ex) {
			Logger.getLogger(VRP.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

}

class Node 
{
	int x;
	int y;
	int demand;
	int ID;
	
	// true/false flag indicating if a customer has been inserted in the solution
	boolean isRouted; 

	Node() 
	{
            isRouted = false; 
	}
}

class Solution 
{
	double cost;
	Route rt;
        ArrayList <Route> sr;

	Solution ()
	{
	        sr = new ArrayList<Route>();
		rt = new Route();
		cost = 0;
	}
}

class Route 
{
	ArrayList <Node> nodes;
	double cost;
	final int capacity = 50; 
    int load = 0; 

	Route() 
	{
		cost = 0;
		
		// A new arraylist of nodes is created
		nodes = new ArrayList<Node>();
	}

}