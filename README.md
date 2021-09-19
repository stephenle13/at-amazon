# Introduction
This automation framework is based on the page object model desgin pattern, which using Java, Selenium, JUnit, and Maven.

Author: Le Phuong Dy (Linkedin: https://www.linkedin.com/in/dy-le-phuong-28a63816b/) 

# Prerequisites 
1. Install Git bash
2. Download the project from github link: https://github.com/stephenle13/at-amazon
```
    - git clone https://github.com/stephenle13/at-amazon
```
3. JDK version: 1.8
4. Download Chromedriver, which is compatible with your current chrome verion and put in C:\selenium\drivers\chromedriver_win32\chromedriver93.exe.

# Project structure
```
---amazon.framework						This project contains all the related classes and methods to facilitate creating automated tests.
       |---core	        				-- package defines abstract class to get web driver based on web driver kind.
       |---util            	   			-- package defines based classes/methods to call from another projects.
	   |								(example: DateUtils.java provides powerful extensions, like: converting string to date with format) 
	   |
---amazon.shop.pageObjects				This project contains components and locator for each page on webshop.
       |---login             			-- package defines credential info (e.g: username, password, etc.)                
       |---pages             			-- package contains:
	   |										-- abstract class for shop base, and common methods can be reused in inherited classes
	   |									    -- page object for each testing page (e.g: LoginPage, ShopPage)
       |                                
---amazon.shop.tests					This project contains all automated test scripts.
       |---amazon.b2c.searchProduct		-- package contains one or more test scenarios, with the same test function.
       |--------PaginatedTests.java	        	-- this is an example test class to verify search feature on Amazon page after loggin in.
              
```

# How to run test
1. Import the project into local IDE (e.g: IntelliJ).
2. Build project and run PaginatedTests.java.


