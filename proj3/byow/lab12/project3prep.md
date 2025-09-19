# Project 3 Prep

**For tessellating hexagons, one of the hardest parts is figuring out where to place each hexagon/how to easily place hexagons on screen in an algorithmic way.
After looking at your own implementation, consider the implementation provided near the end of the lab.
How did your implementation differ from the given one? What lessons can be learned from it?**

Answer:
I haven't seen that one hour, unedited video.
My algorithm generates the map by first dividing it into vertical columns, and then filling those columns with small hexagons.
-----

**Can you think of an analogy between the process of tessellating hexagons and randomly generating a world using rooms and hallways?
What is the hexagon and what is the tesselation on the Project 3 side?**

Answer:
Both involve first generating small rooms/hexagons and then tessellating them.
For Project 3, since each room may be unique, they cannot be tessellated.
Therefore, hallways and inaccessible region are used to establish connections and fill the remaining space.
-----
**If you were to start working on world generation, what kind of method would you think of writing first? 
Think back to the lab and the process used to eventually get to tessellating hexagons.**

Answer:
First think of method that generates single room.
Then tessellate them.
-----
**What distinguishes a hallway from a room? How are they similar?**

Answer:
Different:
hallway: one tile width, connecting different rooms.
room: no width limitation

Similar: 
Both consist with space and wall.

