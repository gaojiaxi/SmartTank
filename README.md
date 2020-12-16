# Introduction
This is the course project for EECE 592 Architectures For Learning Systems.
The high level idea of this project is to create a self-learned Robot in Java. 
This project was built using open-sourced Robocode framework. 
There are three part of the project, which will be discussed as follows.

## Part1. Create a two layer Neural Network to learn a famous non-linear problem (The XOR Problem)
The source code are in BackPropagation.java and BPLearning.java. In order to obtain the results, please run BPLearning.java and modified the Neural Network configuration based on your own experiment. Configurations you may modify includes learning rate, Neural Network architecture, momentum term, number of epoch to train etc.

## Part2. Train a smart tank with Reinforcement Learning using a Look-Up-Table(LUT).

###System Architecture


I define the set of states as a four dimension vector. The four dimensions of state vector are **heading, location,bearing and distance** respectively. Heading represents the heading direction of my robot which is discretized to 4 values: up, right, down, left, as shown in Figure 1. Location represents the location of my robot in the battle field which is discretized to 9 areas as shown in Figure 2. Four corners are 100×100 pixels square areas. Bearing represents the angle of the opponent robot relative to the heading of my robot which is also discretized to 4 value similar to heading. Distance represents the distance between my robot and the opponent robot which is discretized to 5 values: very near(0-100 pixels), near(100-200 pixels), normal(200-300 pixels), far(300-400 pixels), very far( greater than 500 pixels).

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/heading_location.jpg)


I define the set of actions includes three actions: **gravitation movement, anti-gravitation movement and pendulum movement**. I do not use any fire action, so the robot need to choose one of the three actions to dodge the enemy's bullet to win. In terms of immediate reward, it is calculated as the difference between energy change of my robot and that of the opponent robot.

I choose the Corners robot as my opponent robot, set the size of battle field 800×800 pixels and keep other parameters default values.


### Learing Performance Evaluation
####(a) Progress and convergence of learning
For this part, I adopt on-policy learning, set learning rate 0.4, discount factor 0.9, ε 0.05 and run 1000 rounds of battles. For the 1000 battles, the surviving rate of my robot is 28/1000=2.8%, as shown in Figure 3.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure3.jpg)

After training my robot 1000 rounds, I set ε=0 and run 1000 rounds to test the learning performance.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure4.jpg)

From Figure 4, we can see my robot demonstrates a remarkable progress in surviving rate attaining 822/1000=82.2% after 1000 rounds of battles training.
From another perspective, we can also see the progress of my robot by cumulative reward. In the program, I calculate the cumulative reward and reset it once every 200000 turns. Figure 5 reflects the variation of cumulative reward over the entire learning and test process. After 1000 rounds of battle training, the cumulative reward converges to between 2000 and 4000.




## Installation
Clone the GitHub repository and then import Smart-Event.war into your eclipse.

```
git clone https://github.com/gaojiaxi/SmartEvent.git
```
In order to import the WAR file into Eclipse JEE, click on File -> Import. Select Web -> WAR File.
* **WAR** file: Provide the full path of the WAR file on your computer.
* **Web project**: This will be auto-filled based on the WAR file name. You may change it depends on your own settings.
* **Target runtime**: You will need to select “Apache Tomcat 9.0”. The first time you import a WAR
file (or create new “Dynamic Web Project”) you will need to declare the new runtime environment. Do this by clicking on “New” and filling in the form as follows:
	* **Apache Tomat v9.0**, then click “Next”
	* Provide the Tomcat installation directory by giving the full pathname of the directory
containing your unzipped version of Tomcat 9.0.
	* Click “Finished”.
* Click "Finished".

Run the imported project by “right-clicking” on the new project and selecting “Run As -> Run on Server. <br>


## Screenshots
logic/high level structure of this project
![](https://github.com/gaojiaxi/SmartEvent/tree/master/demoPictures/highLevelStructure.jpg)
nearby page
![](https://github.com/gaojiaxi/SmartEvent/tree/master/demoPictures/nearby.jpg)
favorite page
![](https://github.com/gaojiaxi/SmartEvent/tree/master/demoPictures/favorite.jpg)
recommendation page
![](https://github.com/gaojiaxi/SmartEvent/tree/master/demoPictures/recommendation.jpg)


## Todo list
1. The login system
2. The registration system.
3. Booting front page using AugularJS or React.

## Deployment
Deployment Environment: Amazon EC2 <br>
(Please contact me at jiaxig@ece.ubc.ca if any issue happend)

## Change Log
v1.0.0(11/02/2019)<br>
* user can see nearby events based on their geo-location
* user can like/unlike events by clicking the 'Heart' symbol on front page.
* System will recommend events to user based on users liked events and geo-location.