# Scraper

This scraper was written the summer of 2022 and it allows the user to scrape public information from websites, in various type of ways. 
I figured that the tasks might collide and found out that multithreaded programming was an actual thing, whereas I took this into consideration
without implementing it. A full year after the scraper was written, alot of improvements comes to mind when reviewing this code again. Aside from 
esthetics, here are some points to improve.

First off; multithreading. I remember that the network card can only process one new website at a time, which means that that resource is allocated 
all the time in the case of multiple websites being scraped. Multithreading would further allow optimal allocation of resources, letting the processing of
data and writing into the excel file happen whilst gathering info from the next website. Multithreading was taught the semester following summer 2022, and I'm
now confident that this could be better.

Secondly; structure. In multiple courses following, we worked both in groups and individually to construct larger projects. This is where we really got to see
the strength in building classes and their methods logically. How to decrease dependency between them yet letting them cooperate efficiently. Rather using
more classes each with their designated responsibilities, even implementing superclasses. This is not a big program, but the wall of code is ugly to any
programmer. This could easily be divided and simplified, some solutions in the code can even be replicated with the use of objects. For example,
keeping the App class pure from processing data by keeping the JFrames and the button triggers, but calling the processing functions from another class.
I remember how dissatisfied I was with the use of arrays in the processing of data, surely there is something better. Perhaps making an object with attributes instead of arrays.
