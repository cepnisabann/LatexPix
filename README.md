# LatexPix
Android OCR application for solving math equations.
In this project i tried to create a reliable application for solving mathematical equations by performing OCR on mathematical expressions. One of the disadvantages of my system is that it requires calibration to read a specific font. The current version of the application works well on characters written in fonts similar to the one of the templates. 
Tests done using the application:
	
| Difficulty | Total Test Number | Correctly Analyzed Equation Number | Correctly Solved Equation Number | Correctly Analyzed Percentage | Correctly Solved Percentage |
|------------|------------------|-----------------------------------|---------------------------------|------------------------------|----------------------------|
| Simple     | 20               | 19                                | 19                              | 95%                          | 95%                        |
| Normal     | 15               | 14                                | 14                              | 93.33%                       | 93.33%                     |
| Complex    | 15               | 13                                | 12                              | 86.67%                       | 80%                        |
| Total      | 50               | 46                                | 45                              | 92%                          | 90%                        |


As you can see we have correctly analyzed 92 percent of all equations. Keep in mind font picture quality and etc. Can effect this percentage greatly. Still its a high percentage. Correctly solved percentage is 2 percent lower than analyzed cause of the LaTex MathML faults. 


Some Examples to Simple Normal Complex are;
![image](https://github.com/cepnisabann/LatexPix/assets/34573420/d31fe671-8d1f-468d-b18c-315d71d33370)
