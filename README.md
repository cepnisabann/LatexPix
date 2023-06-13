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


![Main Screen without Image](https://github.com/cepnisabann/LatexPix/assets/34573420/3af6fe7e-e043-4979-88fd-04c757d3eeb5)
![Only Analyzed Screen](https://github.com/cepnisabann/LatexPix/assets/34573420/c45ec470-0f83-4f6c-8b7a-deecffd4f16d)
![Analyzed And Solved Screen](https://github.com/cepnisabann/LatexPix/assets/34573420/935d868f-c98e-42c5-9365-48b08cbc2911)


Api's that i used for my android applications are
OCR API: https://github.com/lukas-blecher/LaTeX-OCR

For LaTex to MathML conversation: https://github.com/fred-wang/TeXZilla


Simple application stages


![SimpleWorkFlow](https://github.com/cepnisabann/LatexPix/assets/34573420/5874691c-5d9e-423a-9744-04d9c052dce9)


How API’s and application interact with each other


![WorkFlowdiagram](https://github.com/cepnisabann/LatexPix/assets/34573420/393f1bed-33e1-4ef2-95b0-7cee365fe693)

I do not own any of the api modules. Thank you.
