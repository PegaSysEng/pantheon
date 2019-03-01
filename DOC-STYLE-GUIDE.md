# Pantheon Documentation Style Guide

## Purpose of this Document

This document contains guidelines to help the Pantheon documentation contributor make the user 
experience as nice as possible by providing a coherent documentation with comprehensive examples and
well organised content.

This is a living document and it should evolve to better suit Pantheon users and contributors needs.

> **Note:** Although not every pattern shown in this style guide is currently followed in Pantheon 
documention, it is our intention to revise and write new parts with these guidelines in mind.

**The primary audience for this document is:**

*   Members of the Pantheon team
*   Developers and technical writers contributing to the Pantheon project

## Mission Statement

The Pantheon documentation should create a consistent and easy to understand experience for end users.
We're focused on creating a great experience for both new and expert users of Ethereum clients.

## General Guidelines

There are four guiding principles for the Pantheon documentation to help us create a good 
experience for both new and expert users: 
1. be consistent,
1. keep it simple but technically correct,
1. be proactive and suggest good practices,
1. be informative and exhaustive.

This section outlines what each of these principles mean and the following sections explain how 
these principles should be applied in specific scenarios.     

### 1. Be Consistent

Consistency is important to help our end users build a mental model of how Pantheon works.
By being consistent with our word choices, visual formatting, and style of communication it helps 
users know what to expect as they refer or search Pantheon documentation.  

### 2. Keep It Simple But Technically Correct

Avoid technical jargon and always assume our end users may not have all the knowledge about Ethereum.

It doesn't mean explaining all Ethereum technology in our documentation, but it does mean explaining 
things in a simple way and when complexity inevitably rises, directing our users to some resources 
that will help them.

Example: If you feel the need to explain how EVM works, provide a link to the ethdocs.org 
documentation such as 
http://ethdocs.org/en/latest/introduction/what-is-ethereum.html#ethereum-virtual-machine

Also, simplifying doesn't mean providing incorrect information.
If you think some details may confuse readers, simply provide the details in another page or in a 
detail section or link to a detailed explanation such as a yellow paper.

### 3. Be Proactive And Suggest Good Practices

Being proactive means anticipating user needs and guiding them through a process.
This most often takes the form of notes or tip messages alongside the main explanation.
Put yourself in the user's shoes and consider what questions you would have every time you are 
reading the documentation.
If something is implicit, write it, nothing should stay unsaid.

Pushing good practices is also important.
For instance, telling users to secure private keys or to be careful to protect RPC endpoints
in a production environment is one of the main best practices. 

### 4. Be Informative And Exhaustive

We seek a balance between providing enough relevant information to help our users develop a solid 
mental model of how Pantheon works without forcing them to read too much text.

To do so, using "detail" sections or footnotes is recommended.

## Writing Style Guide

We'll use the [Microsoft Style Guide](https://docs.microsoft.com/en-us/style-guide/welcome/) 
as our general guide to writing technical documentation.
We take guidance from it, but are not committing to every rule in it.
For example, we use title case rather than sentence case, as Microsoft does.

From the website we can read : 
> The Microsoft Writing Style Guide replaces the Microsoft Manual of Style, 
a respected source of editorial guidance for the tech community for more than 20 years.
The style guide features updated direction and new guidance for subjects that weren't around when 
the last edition released.
It's a reimagining of the Microsoft style, in a way that's natural, simple, and clear.

Here are some important points that we follow in these guidelines :
 
### Active voice
Use active voice when possible. Address the second person singular (you); 
this creates a more personal friendly style and avoids using gender pronouns (he or she).

### Contractions
Use contractions. (don’t, won’t)

Use common contractions, such as it’s, you’re, that's, and don’t, to create a friendly, informal tone.

### Recommend
It's acceptable to use "we recommend" to introduce a product recommendation.
Don't use "PegaSys recommends" or "it is recommended."

Example: this is not recommended for production code -> we don't recommend this for production code

### Directory vs Folder 
Prefer "directory" over "folder" as we are writing for developers. 

### Title Case For Headings
Use title case for headings.

Note: This is a case where we are not following the Microsoft Writing Style Guide. 

### Assumed Knowledge For Readers
We have two distinct audiences that we need to consider when developing different sorts of content :

- new to Ethereum and Ethereum clients,
- experienced with Ethereum clients other than Pantheon.

### Avoid Abbreviations

Try not tu use abbreviations [except for well known ones and some jargon](#abbreviations).
Don't use "e.g." but use "for example".
Don't use "i.e." but use "that is".