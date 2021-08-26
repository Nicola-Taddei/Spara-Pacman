import java.awt.*;  
import java.io.*;
import javax.swing.JFrame;  
import java.lang.Thread;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.lang.Math;
import java.lang.Comparable;
import java.text.DecimalFormat;
import java.io.IOException;
import java.io.FileNotFoundException;

class Node implements Comparable<Node>
{
	public final int direction;
	public int x;
	public int y;
	public double weight;
	public Node(final int dir, int x, int y, double w)
	{
		this.x = x;
		this.y = y;
		this.weight = w;
		this.direction = dir;
	}
	public int compareTo(Node n)
	{
		if(this.weight > n.weight)
			return 1;
		if(this.weight < n.weight)
			return -1;
		if(this.weight == n.weight)
			return 0;
		return 0;
	}
}

public class Test
{
	

	public static class EuristicPriority implements Comparator<Node>
	{
		public int compare(Node n1, Node n2)
		{
			if(n1.weight > n2.weight)
				return 1;
			if(n1.weight == n2.weight)
				return 0;
			if(n1.weight < n2.weight)
				return -1;

			return 0;
		}
	}

	public static class Block
	{
		public static final int WALL = 1;
		public static final int VOID = 2;
	}

	public static class Cinematics
	{
		public double x;				//number of cell
		public double y;				//number of cell
		public double speed_x;			//in number of cells per unit of time (clocks)
		public double speed_y;			//in number of cells per unit of time (clocks)
		public Cinematics(double x, double y, double speed_x, double speed_y)
		{
			this.x = x;
			this.y = y;
			this.speed_x = speed_x;
			this.speed_y = speed_y;
		}

		public static double distance(Cinematics c1, Cinematics c2)
		{
			return Math.sqrt(Math.pow(c1.x - c2.x, 2) + Math.pow(c1.y - c2.y, 2));
		}

		public Cinematics clone()
		{
			return new Cinematics(x, y, speed_x, speed_y);
		}
	}

	public static class Character
	{
		public static double STD_SPEED = 0.2;
		protected final int MAX_LIFE_POINTS;
		protected int lifePoints;
		protected Cinematics cinematics;
		public final double RADIUS;

		public Character(int lifePoints, Cinematics initialCinematics, double radius)
		{
			this.MAX_LIFE_POINTS = lifePoints;
			this.lifePoints = lifePoints;
			this.cinematics = initialCinematics;
			this.RADIUS = radius;
		}

		public Character clone()
		{
			Character c = new Character(
				lifePoints,
				cinematics.clone(),
				RADIUS
			);
			return c;
		}

		public void setLifePoints(int lp)
		{
			lifePoints = lp;
		}

		public void setCinematics(Cinematics c)
		{
			cinematics = c;
		}

		public int getLifePoints()
		{
			return lifePoints;
		}

		public Cinematics getCinematics()
		{
			Cinematics c = new Cinematics(0, 0, 0, 0);
			c.x = cinematics.x;
			c.y = cinematics.y;
			c.speed_x = cinematics.speed_x;
			c.speed_y = cinematics.speed_y;
			return c;
		}

		public void directionUp()
		{
			cinematics.speed_x = 0;
			cinematics.speed_y = -STD_SPEED;
		}

		public void directionDown()
		{
			cinematics.speed_x = 0;
			cinematics.speed_y = STD_SPEED;
		}

		public void directionLeft()
		{
			cinematics.speed_x = -STD_SPEED;
			cinematics.speed_y = 0;
		}

		public void directionRight()
		{
			cinematics.speed_x = STD_SPEED;
			cinematics.speed_y = 0;
		}

		public void speedRegulation(double steps_x, double steps_y)
		{
			cinematics.speed_x += steps_x;
			cinematics.speed_y += steps_y;
		}

		public void positionRegulation(double steps_x, double steps_y)
		{
			cinematics.x += steps_x;
			cinematics.y += steps_y;
		}

		public int leftCellX(double x)
		{
			if((double)((int)x) + 0.5 < x)
				return (int)x;
			if((double)((int)x) + 0.5 >= x)
				return (int)x - 1;

			return (int)x;  //????
		}

		public int rightCellX(double x)
		{
			if((double)((int)x) + 0.5 <= x)
				return (int)x + 1;
			if((double)((int)x) + 0.5 > x)
				return (int)x;

			return (int)x;   //????
		}

		public int upCellY(double y)
		{
			if((double)((int)y) + 0.5 < y)
				return (int)y;
			if((double)((int)y) + 0.5 >= y)
				return (int)y - 1;

			return (int)y;   //????
		}

		public int downCellY(double y)
		{
			if((double)((int)y) + 0.5 <= y)
				return (int)y + 1;
			if((double)((int)y) + 0.5 > y)
				return (int)y;

			return (int)y;   //????
		}

		public boolean isOccupable(int[][] map, double thisX, double thisY)
		{
			double[] y_jumps = {+RADIUS, +RADIUS, -RADIUS, -RADIUS};
			double[] x_jumps = {-RADIUS, +RADIUS, +RADIUS, -RADIUS};

			if(map[(int)thisY][(int)thisX] == Block.WALL)
				return false;
			for(int j = 0; j < 4; j++)
			{
				if(map[(int)(thisY + y_jumps[j])][(int)(thisX + x_jumps[j])] == Block.WALL)
					return false;
			}

			return true;
		}

		public void move(final int[][] map, final int H, final int W)   //move for one clock
		{
			double newX = cinematics.x + cinematics.speed_x;
			double newY = cinematics.y + cinematics.speed_y;
			if(newX < 0.5 || newX >= ((double)W - 0.5) || newY < 0.5 || newY >= ((double)H - 0.5))
				setCinematics(new Cinematics(cinematics.x, cinematics.y, 0, 0));

			//if(map[(int)(newY + 0.499999)][(int)(newX)] == Block.WALL || map[(int)(newY - 0.499999)][(int)(newX)] == Block.WALL || map[(int)(newY)][(int)(newX + 0.499999)] == Block.WALL || map[(int)(newY)][(int)(newX - 0.499999)] == Block.WALL || map[(int)(newY - 0.499999)][(int)(newX - 0.499999)] == Block.WALL || map[(int)(newY - 0.499999)][(int)(newX + 0.499999)] == Block.WALL || map[(int)(newY + 0.499999)][(int)(newX - 0.499999)] == Block.WALL || map[(int)(newY + 0.499999)][(int)(newX + 0.499999)] == Block.WALL)
				//setCinematics(new Cinematics(cinematics.x, cinematics.y, 0, 0));
			if(!isOccupable(map, newX, newY))
				setCinematics(new Cinematics(cinematics.x, cinematics.y, 0, 0));
				
			positionRegulation(cinematics.speed_x, cinematics.speed_y);	
		}

		public boolean lifePointsRegulation(int steps)
		{
			int lp = this.lifePoints + steps;
			if(lp > MAX_LIFE_POINTS)
				lifePoints = MAX_LIFE_POINTS;
			else if(lp <= 0)
				return false;
			else
				lifePoints = lp;

			return true;
		}
	}

	public static class Size
	{
		public static final int SMALL = 1;
		public static final int MEDIUM = 2;
		public static final int BIG = 3;
	}

	public static class Direction
	{
		public static final int UP = 0;
		public static final int DOWN = 1;
		public static final int RIGHT = 2;
		public static final int LEFT = 3;
	}

	public static class Mob extends Character
	{
		public static final double MOB_RADIUS = 0.4;
		public final int SIZE; 
		private Random rand = new Random();
		public static EuristicPriority ep;
		
		public static int lp(int size)
		{
			switch(size)
			{
				case Size.SMALL: 
					return 2; 
				case Size.MEDIUM: 
					return 3;
				case Size.BIG: 
					return 4; 
				default: 
					return 2;
			}
		}

		public Mob(final int size, Cinematics initialCinematics)
		{
			super(lp(size), initialCinematics, MOB_RADIUS);
			this.SIZE = size;
		}

		public Mob clone()
		{
			Mob m = new Mob(SIZE, cinematics.clone());
			return m;
		}

		private double euristics(final int thisX, final int thisY, final int objX, final int objY)
		{
			return Math.sqrt(Math.pow(thisX - objX, 2) + Math.pow(thisY - objY, 2));
		}

		private double cut(double n, int d)   //d --> numero di cifre decimali
		{
			if(n * Math.pow(10, d) <= (double)((int)(n * Math.pow(10, d)) + 0.5))
				return (double)((int)(n * Math.pow(10, d)) / Math.pow(10, d));
			else
				return (double)((int)(n * Math.pow(10, d) + 1) / Math.pow(10, d));

			//return (double)((int)(n * Math.pow(10, d)) / Math.pow(10, d));
		}

		public void AI(final int[][] map, final int H, final int W, final int x, final int y)   //da modificare
		{
			//System.out.println("AI():");
			int direction = Direction.UP;
			int[][] weights = new int[H][W];
			//int[][] D = new int[H][W];

			/*
			for(int i = 0; i < H; i++)
				for(int j = 0; j < W; j++)
					D[i][j] = 9;
			*/

			for(int i = 0; i < H; i++)
				for(int j = 0; j < W; j++)
					weights[i][j] = H*W;

			double currentX = cinematics.x;
			double currentY = cinematics.y;

			//weights[(int)currentY][(int)currentX] = 0;
			PriorityQueue<Node> queue = new PriorityQueue<Node>(H*W, ep);

			//System.out.println(currentX +" " + currentY);

			if(isOccupable(map, currentX + STD_SPEED, currentY))
			{
				queue.add(new Node(Direction.RIGHT, rightCellX(cut(currentX, 1)), (int)currentY, euristics(rightCellX(cut(currentX, 1)), (int)currentY, x, y)));
				weights[(int)currentY][rightCellX(cut(currentX, 1))] = 0;

				//D[(int)currentY][rightCellX(cut(currentX, 1))] = Direction.RIGHT;
				//System.out.println("RIGHT");
				//System.out.println("RIGHT_CELL:  " + rightCellX(cut(currentX, 1)) + " " + (int)currentY);
			}
			if(isOccupable(map, currentX - STD_SPEED, currentY))
			{
				queue.add(new Node(Direction.LEFT, leftCellX(cut(currentX, 1)), (int)currentY, euristics(leftCellX(cut(currentX, 1)), (int)currentY, x, y)));
				weights[(int)currentY][leftCellX(cut(currentX, 1))] = 0;

				//D[(int)currentY][leftCellX(cut(currentX, 1))] = Direction.LEFT;
				//System.out.println("LEFT");
				//System.out.println("LEFT_CELL:  " + leftCellX(cut(currentX, 1)) + " " + (int)currentY);
			}
			if(isOccupable(map, currentX, currentY + STD_SPEED))
			{
				queue.add(new Node(Direction.DOWN, (int)currentX, downCellY(cut(currentY, 1)), euristics((int)currentX, downCellY(cut(currentY, 1)), x, y)));
				weights[downCellY(cut(currentY, 1))][(int)currentX] = 0;

				//D[downCellY(cut(currentY, 1))][(int)(currentX)] = Direction.DOWN;
				//System.out.println("DOWN");
				//System.out.println("DOWN_CELL:  " + (int)currentX + " "+ downCellY(cut(currentY, 1)));
			}
			if(isOccupable(map, currentX, currentY - STD_SPEED))
			{
				queue.add(new Node(Direction.UP, (int)currentX, upCellY(cut(currentY, 1)), euristics((int)currentX, upCellY(cut(currentY, 1)), x, y)));
				weights[upCellY(cut(currentY, 1))][(int)currentX] = 0;

				//D[upCellY(cut(currentY, 1))][(int)(currentX)] = Direction.UP;
				//System.out.println("UP");
				//System.out.println("UP_CELL:  " + (int)currentX + " " + upCellY(cut(currentY, 1)));
			}

			/*for(int i = 0; i < H; i++)
			{
				for(int j = 0; j < W; j++)
				{
					System.out.print(D[i][j] + " ");
				}
				System.out.println("");
			}

			System.out.println("\n");*/

			//System.out.println("If iniziali");

			Node temp;

			while(queue.size() != 0)
			{
				temp = queue.poll();
				//System.out.println("Cicle");
				//System.out.println(temp.x + " " + temp.y);

				if(temp.x != x || temp.y != y)
				{
					if(temp.x + 1 >= 0 && temp.x + 1 < W && temp.y >= 0 && temp.y < H)
						if(map[temp.y][temp.x + 1] == Block.VOID && weights[temp.y][temp.x] + 1 < weights[temp.y][temp.x + 1])
						{
							if(temp.x + 1 != x || temp.y != y)
								queue.add(new Node(temp.direction, temp.x + 1, temp.y, euristics(x, y, temp.x + 1, temp.y)));
							else
							{
								direction = temp.direction;
								//System.out.println(direction);
							}
							weights[temp.y][temp.x + 1] = weights[temp.y][temp.x] + 1;

							//D[temp.y][temp.x + 1] = temp.direction;
							//System.out.println(1);
						}

					if(temp.x - 1 >= 0 && temp.x - 1 < W && temp.y >= 0 && temp.y < H)
						if(map[temp.y][temp.x - 1] == Block.VOID && weights[temp.y][temp.x] + 1 < weights[temp.y][temp.x - 1])
						{
							if(temp.x - 1 != x || temp.y != y)
								queue.add(new Node(temp.direction, temp.x - 1, temp.y, euristics(x, y, temp.x - 1, temp.y)));
							else
							{
								direction = temp.direction;
								//System.out.println(direction);
							}
							weights[temp.y][temp.x - 1] = weights[temp.y][temp.x] + 1;

							//D[temp.y][temp.x - 1] = temp.direction;
							//System.out.println(2);
						}

					if(temp.x >= 0 && temp.x < W && temp.y + 1 >= 0 && temp.y + 1 < H)
						if(map[temp.y + 1][temp.x] == Block.VOID && weights[temp.y][temp.x] + 1 < weights[temp.y + 1][temp.x])
						{
							if(temp.x != x || temp.y + 1 != y)
								queue.add(new Node(temp.direction, temp.x, temp.y + 1, euristics(x, y, temp.x, temp.y + 1)));
							else
							{
								direction = temp.direction;
								//System.out.println(direction);
							}
							weights[temp.y + 1][temp.x] = weights[temp.y][temp.x] + 1;

							//D[temp.y + 1][temp.x] = temp.direction;
							//System.out.println(3);
						}

					if(temp.x >= 0 && temp.x < W && temp.y - 1 >= 0 && temp.y - 1 < H)
						if(map[temp.y - 1][temp.x] == Block.VOID && weights[temp.y][temp.x] + 1 < weights[temp.y - 1][temp.x])
						{
							if(temp.x != x || temp.y - 1 != y)
								queue.add(new Node(temp.direction, temp.x, temp.y - 1, euristics(x, y, temp.x, temp.y - 1)));
							else
							{
								direction = temp.direction;
								//System.out.println(direction);
							}
							weights[temp.y - 1][temp.x] = weights[temp.y][temp.x] + 1;

							//D[temp.y - 1][temp.x] = temp.direction;
							//System.out.println(4);
						}
					//System.out.println("If annidati nel while");
				}

				/*
				for(int i = 0; i < H; i++)
				{
					for(int j = 0; j < W; j++)
					{
						System.out.print(D[i][j] + " ");
					}
					System.out.println("");
				}

				System.out.println("\n");
				*/
			}

			switch(direction)
			{
				case Direction.UP:
					directionUp();
					//System.out.println("UP");
					break;
				case Direction.DOWN:
					directionDown();
					//System.out.println("DOWN");
					break;
				case Direction.LEFT:
					directionLeft();
					//System.out.println("LEFT");
					break;
				case Direction.RIGHT:
					directionRight();
					//System.out.println("RIGHT");
					break;
				default:
					break;
			}

			/*
			for(int i = 0; i < H; i++)
			{
				for(int j = 0; j < W; j++)
				{
					System.out.print(weights[i][j] + " ");
				}
				System.out.println("");
			}

			System.out.println("\n");

			System.out.println("");
			*/

			/*int n = rand.nextInt(30);
			switch(n)
			{
				case 0:
					directionUp();
					break;
				case 1:
					directionDown();
					break;
				case 2:
					directionLeft();
					break;
				case 3:
					directionRight();
					break;
				default:
					break;
			}*/
		}

	}

	public static class MainCharacter extends Character
	{
		public static final double MAIN_CHARACTER_RADIUS = 0.499999;
		public final int RECOVERY_PER_CLOCK;

		public MainCharacter(int lifePoints, Cinematics initialCinematics, int rpc)
		{
			super(lifePoints, initialCinematics, MAIN_CHARACTER_RADIUS);
			RECOVERY_PER_CLOCK = rpc;
		}

		public boolean lifePointsRegulation(int steps)
		{
			return super.lifePointsRegulation(steps + RECOVERY_PER_CLOCK);
		}

		public MainCharacter clone()
		{
			MainCharacter m = new MainCharacter(lifePoints, cinematics.clone(), RECOVERY_PER_CLOCK);
			return m;
		}

	}

	
	public static abstract class SpawnPoint
	{
		//public final Character[] characters;
		public int clockCounter;
		public final int x;
		public final int y;
		protected final Cinematics initialCinematics;
		protected final int SPAWN_DELAY; //number of clocks between every spawn
		protected boolean isActive;

		public SpawnPoint(final int x, final int y, final int spawnDelay)
		{
			this.clockCounter = 0;
			this.x = x;
			this.y = y;
			this.SPAWN_DELAY = spawnDelay;
			this.isActive = false;			//the spawnpoint is initially inactive
			initialCinematics = new Cinematics(x + 0.5, y + 0.5, 0, 0);
		}

		public boolean getStatus()
		{
			return isActive;
		}

		public void activate()
		{
			isActive = true;
		}

		public void inactivate()
		{
			isActive = false;
		}
	}

	public static Mob[] predefinedMobs = new Mob[]
	{
		new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.MEDIUM, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.MEDIUM, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.MEDIUM, new Cinematics(0, 0, 0, 0)),
		new Mob(Size.BIG, new Cinematics(0, 0, 0, 0))
	};

	public static class MobSpawnPoint extends SpawnPoint
	{
		private Mob[] characters = new Mob[]
		{
			new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.MEDIUM, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.MEDIUM, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.MEDIUM, new Cinematics(0, 0, 0, 0)),
			new Mob(Size.BIG, new Cinematics(0, 0, 0, 0))
		};
		private Mob m;
		public final int CHARACTER_NUMBER = 10;
		public MobSpawnPoint(final int x, final int y, final int spawnDelay)
		{
			super(x, y, spawnDelay);
			//CHARACTER_NUMBER = 10;
			//characters = new Mob[CHARACTER_NUMBER];
			//for(int i = 0; i < CHARACTER_NUMBER; i++)
				//characters[i] = predefinedMobs[i];
			//characters = predefinedMobs;
		}

		public Mob getMobReference()
		{
			return m;
		}

		public boolean trySpawn()
		{
			clockCounter++;
			Random rand = new Random();
			if(clockCounter >= SPAWN_DELAY)
			{
				clockCounter = 0;
				m = characters[rand.nextInt(CHARACTER_NUMBER)].clone();
				m.setCinematics(initialCinematics.clone());
				return true;
			}
			return false;
		}
	}

	public static class MainSpawnPoint extends SpawnPoint
	{
		private MainCharacter CHARACTER;
		public MainSpawnPoint(final int x, final int y, final int spawnDelay, MainCharacter m)
		{
			super(x, y, spawnDelay);
			CHARACTER = m;
		}

		public MainCharacter spawn()
		{
			MainCharacter mc = CHARACTER.clone();
			mc.setCinematics(initialCinematics.clone());
			return mc;
		}
	}
	

	public static class Map
	{
		public final int HEIGHT;
		public final int WIDTH;
		public final int LEVEL;
		private final int MAX_N_FIREBALLS = 100;
		private final int MAX_N_MOBS = 10;
		public final double FIREBALL_SPEED = 0.3;
		public final double FIREBALL_RADIUS = 0.1;
		public int currentNMobs;
		public int currentNFireballs;
		public MainCharacter mainCharacter;
		public Mob[] mobs = new Mob[MAX_N_MOBS];
		public Cinematics[] fireballCinematics = new Cinematics[MAX_N_FIREBALLS];
		public final int[][] MAP;
		public MainSpawnPoint MSP;
		public MobSpawnPoint[] MOBSP = new MobSpawnPoint[100];
		public int MOBSP_DIM = 0;  //temporaneo
		private Mob tempMob;

		public Map()
		{
			LEVEL = 1;   //????
			FileReader f;
			this.HEIGHT = 25;
			this.WIDTH = 25;

			MAP = new int[HEIGHT+5][WIDTH+5];

			int temp_mobsp_dim = 0;
			//int counter = 0;
			//int voids = 0;
			//int n = 0;
			int tmp = 0;
			//int meno_1 = 0;

			char c = '#';

			try{
				f = new FileReader("Mappa.txt");

				//System.out.println("File aperto");
			

				for(int i = 0; i < this.HEIGHT; i++)
				{
					for(int j = 0; j < this.WIDTH; j++)
					{
						try{
							tmp = f.read();
							//if(tmp == -1)
								//meno_1++;
							c = (char)tmp;
						}catch(IOException e){}
						//System.out.print(tmp + " ");
						switch(c)
						{
							case '#':
								//n++;
								MAP[i][j] = Block.WALL;
								break;
							case '@':
								//n++;
								//voids++;
								MAP[i][j] = Block.VOID;
								break;
							case '?':
								//n++;
								MAP[i][j] = Block.VOID;
								temp_mobsp_dim++;
								MOBSP[temp_mobsp_dim - 1] = new MobSpawnPoint(j, i, 100);
								break;
							case '+':
								//n++;
								MAP[i][j] = Block.VOID;
								MSP = new MainSpawnPoint(j, i, 0, new MainCharacter(10, new Cinematics((double)j + 0.5, (double)i + 0.5, 0, 0), 0));
								break;
							//case '\n':
								//j--;
								//break;
							//case '\0x000b':
								//j--;
								//break;
							default:
								j--;
								//counter++;
								break;
						}
					}
					//System.out.println("");
				}

				//System.out.println("Unknown: " + counter);
				//System.out.println("Meno uno: " + meno_1);
				//System.out.println("Voids: " + voids);
				//System.out.println("Normals: " + n);

				MOBSP_DIM = temp_mobsp_dim;

				/*
				MAP = new int[][]{
					{1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
					{1, 1, 1, 2, 2, 2, 2, 2, 2, 1},
					{1, 1, 2, 2, 1, 2, 1, 1, 2, 1},
					{1, 1, 2, 1, 1, 2, 2, 2, 2, 1},
					{1, 2, 2, 2, 1, 2, 2, 1, 2, 1},
					{1, 2, 1, 2, 2, 2, 1, 1, 2, 1},
					{1, 2, 1, 1, 1, 1, 1, 2, 2, 1},
					{1, 2, 2, 2, 1, 1, 2, 2, 1, 1},
					{1, 1, 2, 2, 2, 2, 2, 1, 1, 1},
					{1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
				};
				*/

				mainCharacter = MSP.spawn();
				currentNMobs = 0;
				currentNFireballs = 0;

			}catch(FileNotFoundException e){}
			
			//tempMob = MOBSP.getMobReference();
			//mobs[0] = new Mob(Size.SMALL, new Cinematics(2.5, 2.5, 0, 0));

		}

		public Map(final int HEIGHT, final int WIDTH, final int level)
		{
			LEVEL = level;   
			FileReader f;
			this.HEIGHT = HEIGHT;
			this.WIDTH = WIDTH;

			MAP = new int[HEIGHT+5][WIDTH+5];

			int temp_mobsp_dim = 0;
			int tmp = 0;
			char c = '#';

			try{
				f = new FileReader("Mappa" + LEVEL + ".txt");

				for(int i = 0; i < this.HEIGHT; i++)
				{
					for(int j = 0; j < this.WIDTH; j++)
					{
						try{
							tmp = f.read();
							c = (char)tmp;
						}catch(IOException e){}
						switch(c)
						{
							case '#':
								MAP[i][j] = Block.WALL;
								break;
							case '@':
								MAP[i][j] = Block.VOID;
								break;
							case '?':
								MAP[i][j] = Block.VOID;
								temp_mobsp_dim++;
								MOBSP[temp_mobsp_dim - 1] = new MobSpawnPoint(j, i, 100);
								break;
							case '+':
								MAP[i][j] = Block.VOID;
								MSP = new MainSpawnPoint(j, i, 0, new MainCharacter(10, new Cinematics((double)j + 0.5, (double)i + 0.5, 0, 0), 0));
								break;
							default:
								j--;
								break;
						}
					}
				}

				MOBSP_DIM = temp_mobsp_dim;

				mainCharacter = MSP.spawn();
				currentNMobs = 0;
				currentNFireballs = 0;

			}catch(FileNotFoundException e){}
		}

		public void generateMobs()
		{
			//Mob temp = new Mob(Size.SMALL, new Cinematics(0, 0, 0, 0));
			for(int i = 0; i < MOBSP_DIM; i++)
			{
				if(currentNMobs < MAX_N_MOBS)
				{
					if(MOBSP[i].trySpawn())
					{
						currentNMobs++;
						tempMob = MOBSP[i].getMobReference();
						mobs[currentNMobs - 1] = tempMob;
					}
				}
			}
		}

		public void newFireball(Cinematics c)
		{
			if(currentNFireballs >= MAX_N_FIREBALLS)
				return;

			currentNFireballs++;
			fireballCinematics[currentNFireballs - 1] = c;
		}

		public void removeFireball(int j)
		{
			for(int i = j; i < currentNFireballs - 1; i++)
			{
				fireballCinematics[i] = fireballCinematics[i + 1].clone();
			}

			currentNFireballs--;
		}

		public void removeMob(int j)
		{
			for(int i = j; i < currentNMobs - 1; i++)
				mobs[i] = mobs[i + 1].clone();

			currentNMobs--;
		}

		public void endGame()
		{
			System.out.println("You've been killed");
		}
	}

	public static Map map = new Map();

	public static class MyCanvas extends Canvas
	{
		public static final Color[] WALL_COLOR = {new Color(100, 0, 100), new Color(0, 0, 0), new Color(0, 100, 0)};

		public static final Color BACKGROUND_COLOR = Color.WHITE;
		public static final Color MOB_COLOR = Color.RED;
		public static final Color MY_COLOR = Color.YELLOW;
		public static final Color FIREBALL_COLOR = new Color(50, 100, 250);
		private double xFactor;
		private double yFactor;

		public void paint(Graphics g) 
		{    
			xFactor = (double)getWidth() / (double)map.WIDTH;
			yFactor = (double)getHeight() / (double)map.HEIGHT;
			setBackground(BACKGROUND_COLOR);      
			//setForeground(FOREGROUND_COLOR);  
			g.setColor(WALL_COLOR[map.LEVEL - 1]);
			for(int i = 0; i < map.HEIGHT; i++)
				for(int j = 0; j < map.WIDTH; j++)
				{
					if(map.MAP[i][j] == Block.WALL)
						g.fillRect((int)(j*xFactor), (int)(i*yFactor), (int)xFactor + 1, (int)yFactor + 1);
				}

			for(int i = 0; i < map.currentNFireballs; i++)
			{
				g.setColor(FIREBALL_COLOR);
				g.fillOval((int)((map.fireballCinematics[i].x - map.FIREBALL_RADIUS) * xFactor), (int)((map.fireballCinematics[i].y - map.FIREBALL_RADIUS) * yFactor), (int)(2*map.FIREBALL_RADIUS*xFactor), (int)(2*map.FIREBALL_RADIUS*yFactor));
			}

			for(int i = 0; i < map.currentNMobs; i++)
			{
				g.setColor(MOB_COLOR);
				g.fillOval((int)((map.mobs[i].getCinematics().x - Mob.MOB_RADIUS)*xFactor), (int)((map.mobs[i].getCinematics().y - Mob.MOB_RADIUS)*yFactor), (int)(2*Mob.MOB_RADIUS*xFactor) - 1, (int)(2*Mob.MOB_RADIUS*yFactor) - 1);
			}

			g.setColor(MY_COLOR);
			g.fillOval((int)((map.mainCharacter.getCinematics().x - 0.5)*xFactor), (int)((map.mainCharacter.getCinematics().y - 0.5)*yFactor), (int)xFactor - 1, (int)yFactor - 1);
		}  
	}

	public static class Command
	{
		public static final int DIR_UP = KeyEvent.VK_W;
		public static final int DIR_DOWN = KeyEvent.VK_S;
		public static final int DIR_LEFT = KeyEvent.VK_A;
		public static final int DIR_RIGHT = KeyEvent.VK_D;
		public static final int FIREBALL_UP = KeyEvent.VK_U;
		public static final int FIREBALL_DOWN = KeyEvent.VK_J;
		public static final int FIREBALL_LEFT = KeyEvent.VK_H;
		public static final int FIREBALL_RIGHT = KeyEvent.VK_K;
	}

	public static class KeyChecker extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent event) {
			//System.out.println("Key pressed");
			Cinematics c;
			int code = event.getKeyCode();
			switch(code)
			{
				case Command.DIR_UP:
					map.mainCharacter.directionUp();
					break;
				case Command.DIR_DOWN:
					map.mainCharacter.directionDown();
					break;
				case Command.DIR_LEFT:
					map.mainCharacter.directionLeft();
					break;
				case Command.DIR_RIGHT:
					map.mainCharacter.directionRight();
					break;
				case Command.FIREBALL_UP:
					c = map.mainCharacter.getCinematics();
					c.speed_x = 0;
					c.speed_y = -map.FIREBALL_SPEED;
					map.newFireball(c);
					break;
				case Command.FIREBALL_DOWN:
					c = map.mainCharacter.getCinematics();
					c.speed_x = 0;
					c.speed_y = map.FIREBALL_SPEED;
					map.newFireball(c);
					break;
				case Command.FIREBALL_LEFT:
					c = map.mainCharacter.getCinematics();
					c.speed_x = -map.FIREBALL_SPEED;
					c.speed_y = 0;
					map.newFireball(c);
					break;
				case Command.FIREBALL_RIGHT:
					c = map.mainCharacter.getCinematics();
					c.speed_x = map.FIREBALL_SPEED;
					c.speed_y = 0;
					map.newFireball(c);
					break;
				default:
					System.out.println("Something else");
					break;
			}
		}
	} 

	public static void main(String args[])
	{
		MyCanvas m = new MyCanvas(); 
        JFrame f=new JFrame();  
        f.add(m);  
		f.addKeyListener(new KeyChecker());
        f.setSize(700,700);  
        //f.setLayout(null);  
        f.setVisible(true);  
		boolean isAlive = true;
		map = new Map(25, 25, 1);
		int killedMobs = 0;
		while(true)
		{
			if(killedMobs > 6)
			{
				map = new Map(25, 25, map.LEVEL + 1);
				killedMobs = 0;
			}

			map.generateMobs();
			for(int i = 0; i < map.currentNMobs; i++)
			{
				//System.out.println("mobs[" + i + "].AI():");
				map.mobs[i].AI(map.MAP, map.HEIGHT, map.WIDTH, (int)map.mainCharacter.getCinematics().x, (int)map.mainCharacter.getCinematics().y);
				map.mobs[i].move(map.MAP, map.HEIGHT, map.WIDTH);
				//System.out.println("\n");
			}

			for(int i = 0; i < map.currentNFireballs; i++)
			{
				map.fireballCinematics[i] = new Cinematics(map.fireballCinematics[i].x + map.fireballCinematics[i].speed_x, map.fireballCinematics[i].y + map.fireballCinematics[i].speed_y, map.fireballCinematics[i].speed_x, map.fireballCinematics[i].speed_y);
				if(map.MAP[(int)map.fireballCinematics[i].y][(int)map.fireballCinematics[i].x] == Block.WALL)
					map.removeFireball(i);

				for(int j = 0; j < map.currentNMobs; j++)
				{
					isAlive = true;
					if(Cinematics.distance(map.fireballCinematics[i], map.mobs[j].getCinematics()) <= map.FIREBALL_RADIUS + Mob.MOB_RADIUS)
						isAlive = map.mobs[j].lifePointsRegulation(-1);

					if(!isAlive)
					{
						map.removeMob(j);
						killedMobs++;
					}
				}
			}

			map.mainCharacter.move(map.MAP, map.HEIGHT, map.WIDTH);

			Cinematics mCC = map.mainCharacter.getCinematics();

			for(int i = 0; i < map.currentNMobs; i++)
			{
				if(Math.abs(mCC.x - map.mobs[i].getCinematics().x) <= 1 && Math.abs(mCC.y - map.mobs[i].getCinematics().y) <= 1)
				{
					map.endGame();
					System.exit(0);
				}
			}


			m.repaint();
			f.repaint();
			try{
			Thread.sleep(100);}catch(InterruptedException e){}
		}
	}
}
