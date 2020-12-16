# Introduction
This is the course project for EECE 592 Architectures For Learning Systems.
The high level idea of this project is to create a self-learned Robot in Java. 
This project was built using open-sourced Robocode framework. 
There are three part of the project, which will be discussed as follows.

## Part1. Create a two layer Neural Network to learn a famous non-linear problem (The XOR Problem)
The source code are in BackPropagation.java and BPLearning.java. In order to obtain the results, please run BPLearning.java and modified the Neural Network configuration based on your own experiment. Configurations you may modify includes learning rate, Neural Network architecture, momentum term, number of epoch to train etc.

## Part2. Train a smart tank with Reinforcement Learning using a Look-Up-Table(LUT).

### System Architecture


I define the set of states as a four dimension vector. The four dimensions of state vector are **heading, location,bearing and distance** respectively. Heading represents the heading direction of my robot which is discretized to 4 values: up, right, down, left, as shown in Figure 1. Location represents the location of my robot in the battle field which is discretized to 9 areas as shown in Figure 2. Four corners are 100×100 pixels square areas. Bearing represents the angle of the opponent robot relative to the heading of my robot which is also discretized to 4 value similar to heading. Distance represents the distance between my robot and the opponent robot which is discretized to 5 values: very near(0-100 pixels), near(100-200 pixels), normal(200-300 pixels), far(300-400 pixels), very far( greater than 500 pixels).

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/heading_location.jpg)


I define the set of actions includes three actions: **gravitation movement, anti-gravitation movement and pendulum movement**. I do not use any fire action, so the robot need to choose one of the three actions to dodge the enemy's bullet to win. In terms of immediate reward, it is calculated as the difference between energy change of my robot and that of the opponent robot.

I choose the Corners robot as my opponent robot, set the size of battle field 800×800 pixels and keep other parameters default values.


### Learing Performance Evaluation
#### (a) Progress and convergence of learning
For this part, I adopt on-policy learning, set learning rate 0.4, discount factor 0.9, ε 0.05 and run 1000 rounds of battles. For the 1000 battles, the surviving rate of my robot is 28/1000=2.8%, as shown in Figure 3.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure3.jpg)

After training my robot 1000 rounds, I set ε=0 and run 1000 rounds to test the learning performance.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure4.jpg)

From Figure 4, we can see my robot demonstrates a remarkable progress in surviving rate attaining 822/1000=82.2% after 1000 rounds of battles training.
From another perspective, we can also see the progress of my robot by cumulative reward. In the program, I calculate the cumulative reward and reset it once every 200000 turns. Figure 5 reflects the variation of cumulative reward over the entire learning and test process. After 1000 rounds of battle training, the cumulative reward converges to between 2000 and 4000.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure5.jpg)

#### (b) On-policy learning vs off-policy learning
First let's look at the comparison of the surviving rate between by on-policy and off-policy learning.
![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure6.jpg)

From Figure 6, we cannot see the distinct difference between on-policy and off-policy learning. Both of them have very good performance. The surviving rate by on-policy learning is 82.2% while that by off-policy learning is 82.7%.
From Figure 7, we can see that during training period on-policy learning has higher cumulative reward, but off-policy converges more quickly.
![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure7.jpg)

#### (c) With immediate reward vs without immediate reward

For this part, I train my robot 1000 rounds of battles with immediate reward and without immediate reward respectively, and then test their respective learning performance by 2000 rounds of battles. From Figure 8, we can see that learning performance without immediate reward is much worse than that with immediate reward as it is manifested that after 1000 rounds of learning with immediate reward the surviving rate of my robot soars to 1622/2000=81.1% while that for without immediate reward is only 434/2000=21.7%. This is also reflected in Figure 9 in which learning without immediate reward has lower initial cumulative reward and converges more slowly.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure8.jpg)
![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure9.jpg)

### Comparison of training performance with different values of ε

For this part, I adopt 4 different values of ε(0.1, 0.05, 0.01, 0) to compare their learning performance. From Figure 10, we can see that all of them have very good learning performance in terms of surviving rate of my robot during test process and the smaller ε is, the higher surviving rate during learning process is. The surviving rate for Learning with ε=0 is the highest(1728/2000=86.4%) during test process while that for ε=0.1 is the
lowest(779/1000=77.9%). From cumulative reward's perspective, greedy method still has the best learning performance, keeping cumulative reward between 2000 and 4000 constantly as shown in Figure 11. We can also find that the smaller ε is, the higher the initial cumulative reward corresponding to surviving rate during learning process is, which is not difficult to understand considering the smaller ε gives my robot fewer opportunities to explore other potential better actions and also fewer chances to incur penalties. As is seen from Figure 11, cumulative reward for all of them converges to between 2000 and 4000. ε-greedy method does not show better learning performance in terms of convergence compared to the greedy method here. This is easy to explain when
we consider that my robot has only three actions and in most cases the pendulum movement is the optimal action. Consequently my robot does not have to explore other potential better actions.

![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure10_0.jpg)
![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure10_1.jpg)
![](https://github.com/gaojiaxi/SmartTank/blob/master/demoPictures/figure11.jpg)


## Part3(To be continued)