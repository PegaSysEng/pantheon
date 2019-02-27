# Pantheon Documentation Style Guide

## Purpose of this Document

This document contains guidelines to ensure the Pantheon documentation is consistent and well organised.

This is a living document and will evolve to better suit Pantheon users and contributors needs.

> **Note:** Although not everything in this style guide is currently followed in the Pantheon 
documention, these guidelines are intended to be applied when writing new content and revising 
existing content.

**The primary audience for this document is:**

*   Members of the Pantheon team
*   Developers and technical writers contributing to the Pantheon documentation

## Mission Statement

The Pantheon documentation contributes to a consistent and easy to understand experience for end users.
We're focused on creating a great experience for both new and expert users of Ethereum clients.

## General Guidelines

The guiding principles for the Pantheon documentation are: 
1. Be consistent
1. Keep it simple but technically correct
1. Be proactive and suggest good practices
1. Be informative and exhaustive.

### 1. Be Consistent

Consistency is important to help our end users build a mental model of how Pantheon works.
By being consistent with our word choices, visual formatting, and style of communication it helps 
users know what to expect as they refer to or search Pantheon documentation.  

### 2. Keep It Simple But Technically Correct

Avoid technical jargon and always assume our end users may not be Ethereum experts.

This doesn't mean explaining all Ethereum technology in our documentation. Explain Pantheon functionality  
and when an understanding of complex Ethereum concepts is required refer users to relevant resources.

Example: To explain how the EVM works, link to ethdocs.org documentation such as 
http://ethdocs.org/en/latest/introduction/what-is-ethereum.html#ethereum-virtual-machine

Simple explanations must still be technically correct.

### 3. Be Proactive And Suggest Good Practices

Being proactive means anticipating user needs and guiding them through a process.
This most often takes the form of notes or tip messages alongside the main explanation.
Put yourself in the user's shoes and consider what questions you would have when reading the documentation.

Do not assume required steps are implied. Err on the side of including them if you are unsure. 

Documenting good practices is also important.
Example: Instruct users to secure private keys and protect RPC endpoints a production environments. 

### 4. Be Informative But Concise 

We seek a balance between providing enough relevant information to help our users develop a solid 
mental model of how Pantheon works without forcing them to read too much text.

To provide additional detail, use sub-sections.

## Writing Style Guide

We use the [Microsoft Style Guide](https://docs.microsoft.com/en-us/style-guide/welcome/) as our general guide 
to writing technical documentation.
We take guidance from it but do not apply every rule.
For example, we use title case rather than sentence case.

The Micrsoft Style Guide aims for natural, simple, and clear communication.

Here are some important points we follow:
 
### Active Voice
Use active voice. Use _you_ to create a more personal friendly style. Avoid gendered pronouns (_he_ or _she_).

### Contractions
Use contractions. For example, don’t.

Use common contractions, such as it’s and you’re, to create a friendly, informal tone.

### Recommend
It's acceptable to use "we recommend" to introduce a product recommendation.
Don't use "PegaSys recommends" or "it is recommended."

Example: Instead of _This is not recommended for production code_ use _We don't recommend this for production code_.

### Directory vs Folder 
Use _directory_ over _folder_ because we are writing for developers. 

### Title Case For Headings
Use title case for headings.

Note: This is a case where we are not following the Microsoft Writing Style Guide. 

### Assumed Knowledge For Readers
We have two distinct audiences to consider when developing content:

- New to Ethereum and Ethereum clients
- Experienced with Ethereum clients other than Pantheon.

### Avoid Abbreviations

Try not to use abbreviations [except for well known ones and some jargon](#abbreviations).
Don't use "e.g." but use "for example".
Don't use "i.e." but use "that is".

## Markdown Syntax

All our documentation is written using the [Markdown](https://daringfireball.net/projects/markdown/) syntax.

However, we use two flavors of this syntax:

- One for pages inside the [/docs](/docs) directory that will be rendered by [MkDocs] described bellow
in the [Installed Markdown Extensions](#installed-markdown-extensions) section.
- Another using the [Github syntax](https://guides.github.com/features/mastering-markdown/) 
for pages outside of this documentation directory. These are mainly files required to comply with 
[GitHub open source community profile rules](https://github.com/PegaSysEng/pantheon/community).

### Formatting Markdown

Final documentation rendering is essential, but Markdown code format is also very important.

Formating the Markdown code helps the reviewers and writers to easily navigate in the code and
review your changes.

A few basic rules applies to our documentation :

- Each file must contain a header composed of [meta-data](https://squidfunk.github.io/mkdocs-material/extensions/metadata/) 
and ended by a specific comment.
    Ex:
    ```markdown
    title: Installation overview
    description: Overview and requirements to install Pantheon
    <!--- END of page meta data -->
    ``` 
- [As for other code](https://google.github.io/styleguide/javaguide.html#s4.4-column-limit),
each line of Markdown code must be limited to 100 columns long to be readable on any editor.
Lines have to be wrapped nicely without cutting the line in the middle of a word. Remember that a
line break will display as a space.
- No HTML markup must be used inside a Markdown document.
We provide [a lot of extensions](#installed-markdown-extensions) that are able to do the same thing 
without HTML. If you think that one is missing, just discuss about it with the team, preferably on 
[Gitter] and we'll decide together if it's worth adding an extension.
- Only one first level title must be present on a page.
- Format tables in a way that they look nice also in the source code.
You can quickly achieve this result by using a tool like http://markdowntable.com/

## Documentation Website

### /docs Directory

The [/docs](/docs) directory in the Pantheon project repository contains all the documentation that 
will be generated into a static HTML website using [MkDocs] and the [Mkdocs Material] theme 
then hosted by [readthedocs.org].

The documentation is automatically updated on the [readthedocs.org] site when you merge a pull-request
in the master branch of Pantheon.

The system also detects tags in the Github repository and takes care of making the latest stable release
and the different version available.

In case of an issue try to contact the maintainers of the 
[Pantheon documentation project](https://readthedocs.org/projects/pantheon/).

### MkDocs Configuration

[MkDocs] is a Python tool that generates a static HTML website that is then published using [readthedocs.org]
and made available at [https://docs.pantheon.pegasys.tech/].

Our [MkDocs] setup uses a [Mkdocs Material] theme to render nice html pages. It also comes with some
very interesting extensions.

[MkDocs] in configured in the [mdkocs.yml](/mkdocs.yml) file.

This file enables the configuration of :

- site meta data and variables
- theme configuration
- pages navigation
- extensions
- plugins

The main part that you will have to modify if you contribute to the documentation is the navigation
part in this YAML file.
You will need to update the "nav" section to add your page and set it's page name.

### Preview The Documentation

It's highly recommended to preview your work locally before pushing your changes within a pull-request.
As the final documentation is build with [MkDocs], you have to build your docs with this tool 
locally in order to make sure that Markdown is correctly understood and displayed.

- [Install Python](https://www.mkdocs.org/#installing-python)
- [Install PIP](https://www.mkdocs.org/#installing-pip)
- Install all the required dependencies :
  ```bash
  pip install -r docs/requirements.txt
  ```
- run the following command in the project directory :
  ```bash
  mkdocs serve
  ```
- follow the link displayed on the output of this command that
looks like `[I 190206 18:48:47 server:298] Serving on http://127.0.0.1:8000`, 
here connect to [http://127.0.0.1:8000]

You can let this doc server run as long as you work on the doc, it will update the local website
automatically when you save your changes in your Markdown files. Alos as most IDEs are automatically 
saving changes, you may just have to switch from your editor to the website to visualise the change.

### Installed Markdown Extensions

>**Important** 
> Extensions are only available for the docs under [/docs](/docs) directory.

As markdown can be a bit limited when it comes to some specific rendering of code or admonitions or 
even math formulas, we configured some extensions to help with this and prevent the use of HTML tags.
Extensions will enable you to use simple Markdown syntax to achieve some complex rendering.

>**Important** 
> Never use HTML tags directly in the Markdown files to try to render content.
For consistency reasons we only allow the specific renderings available in the extensions.

Here is a list of the available extensions:

#### TOC

This extensions doesn't require any action and will automatically display a table of content of the 
current page on the right side of the page. It will take in account titles to the third level (`###`). 
After this third level, titles won't be displayed in the TOC.

This extension will also display a link on the right of any title called "permalink".
This link can be used to point to the title from another website and promote a content.

#### Highlight

This extension enables automatic syntax highlighting of code blocks.
You can defined the prefered language to force the highlighting. If you don't provide the language 
name, it will try to automatically discover it but this can lead to errors. We then require the 
language to be defined.

Ex:
````markdown
```json
{
  "jsonrpc" : "2.0",
  "id" : 51,
  "result" : {
    "startingBlock" : "0x5a0",
    "currentBlock" : "0xad9",
    "highestBlock" : "0xad9"
  }
}
```
```` 

Pygment is used as the implementation for this extension, refer to Pygment website where you can 
find a [list of the supported languages](http://pygments.org/languages/).

#### Include

If you have a content that you want to be repeated on multiple pages, you can create it in a common 
page in and include it in all the pages where you need it to appear.

Ex: to include the content of the "test_accounts.md" page that is in the "/docs/global" 
directory in another page, use the following code :
```markdown
{!global/test_accounts.md!}
```

#### Admonition

The [Admonition extension](https://squidfunk.github.io/mkdocs-material/extensions/admonition/#admonition) 
enables nice info/warning/danger blocs.

Example of Markdown source code :
````markdown
!!! note
    This is a multi line note
    in the Pantheon documentation. 
```` 

You have to respect the 4 spaces indentation of the content to be part of the admonition.

We usually use the following [types](https://squidfunk.github.io/mkdocs-material/extensions/admonition/#types)
in our documentation, but any types available can be used if appropriate :

- **Note** : used to add information about a subject that doesn't directly needs to be taken in account 
to use this specific part.
Ex: "available since v0.8.3"
- **Abstract** : used on the begining of a long article. Also know as "TL;DR" this can help users jump 
into the content knowing that they will find their answer somewhere in the page.
- **Info** : used to detail a specific part of the current documentation.
Ex: "The miner coinbase account is one of the accounts defined in the genesis file."
- **Tip** : used to provide an information that could help improve the use of the tool, make it faster.
Ex: to prevent restarting the tutorial from scratch we display a tip with the following text:
"To restart the private network in the future, start from 4. Restart First Node as Bootnode."
- **Warning** : used to warn the users about something important.
Ex: "This will be deprecated in next version."
- **Danger** : used to alert the user about a potential dangerous effect like a risk of destroying 
something or lose assets.
Ex: "Never use the development private keys for production use".
- **Example** : used to display an example. We usually use it with a single or tabbed code-block:
````markdown
!!! example
    This example shows how to use the `net_listening` RPC method.
    
    ```bash tab="curl HTTP request"
    $ curl -X POST --data '{"jsonrpc":"2.0","method":"net_listening","params":[],"id":53}' <JSON-RPC-http-endpoint:port>
    ```
    
    ```bash tab="wscat WS request"
    {"jsonrpc":"2.0","method":"net_listening","params":[],"id":53}
    ```
    
    ```json tab="JSON result"
    {
      "jsonrpc" : "2.0",
      "id" : 53,
      "result" : true
    }
    ```
```` 

#### Footnotes

You can add footnotes to your page by using the [Footnotes extension](https://squidfunk.github.io/mkdocs-material/extensions/footnotes/).

This will show content at the end of the page and a numbered link inside the text to go to this content.
It will also add a link at the end of the footnote to go back to the text.

#### Definitions List

You can provide definitions list directly in the Markdown by using the syntax of the 
[def_list extension](https://python-markdown.github.io/extensions/definition_lists/)

#### Abbreviations

We tend to restrict the use of abbreviations but some like "PoW" for proof-of-work or "dApp" for 
decentralized application are now parts of the Ethereum jargon. You can however provide a hint on 
these abbreviations by using the [Abbreviation extension](https://python-markdown.github.io/extensions/abbreviations/).

Our style guide requires that abbreviations are placed on the beginning of the Markdown file, just 
after the meta-data header.

Once done, any words matching the abbreviation will appear with a dotted underline and when you 
move focus over them, an interrogation mark will appear with a balloon containing the definition.

Ex:
```markdown
description: This is an example page
<!--- END of page meta data -->

*[PoA]: Proof of Work

Clique is a PoA mechanism used in the Rinkeby test network
```

#### Math

Math formulas can be written in the documentation using the syntax provided by the 
[Arithmatex extension](https://squidfunk.github.io/mkdocs-material/extensions/pymdown/#arithmatex-mathjax ).

Ex:
```markdown
$\sigma=\displaystyle\prod_{k=1}^t\sigma_{i_k}^{L_{i_k}(0)}$

Consctructing the threshold signature $\sigma$ from $t$ individual 
signatures $\sigma_{i_k}$, $k=1,\dots,t$ and the Lagrange polynomials 
$L_{i_1}, \dots,L_{i_t}$ associated to the set $I=\{i_1,\dots,i_t\}$ of signers.
```

#### Better emphasis

The [Betterem extension](https://facelessuser.github.io/pymdown-extensions/extensions/betterem/) is 
automatically applied to your content so you can make your text bold and italic in a smarter way.

#### Keyboard shortcuts

In case you have to explain a keyboard shortcut, use the 
[Keys syntax](https://facelessuser.github.io/pymdown-extensions/extensions/keys/)

Ex:
```markdown
++ctrl+C++
```

#### Folding Details Blocks

You can use a folding block to display details of some content.
This block can be closed by default or open.
This pattern helps reducing the length of content and helps having a faster overview of the whole page.

Ex:
```markdown
???+ note "Folding details"
    This is the detail of my content.
    The plus sign makes it unfolded by default.
    Remove the plus sign and it will be folded by default.
```

#### Emojis

Emojis are fun, but they can also be useful to draw the reader's attention.
Try to use only neutral emojis like :warning:

Refer to a [supported full list of available emojis](https://www.webfx.com/tools/emoji-cheat-sheet/)
to find the suitable code.

Ex:
`:warning:` displays :warning:

#### Magic Links

If you want an URL to be displayed as a link, simply write it, this extension will transform it magically.
You don't need to surround it with Markdown links syntax.

#### Highlight content

To display highlighted content, you can use the 
[Mark extension](https://facelessuser.github.io/pymdown-extensions/extensions/mark/).

Text surrounded by double equals will then be highlighted in yellow.

Ex:
```markdown
==This is highlighted text==
```

#### Striked through text

If you need to indicate something obsolete or a wrong way to do something, you can use the 
[tilde syntax](https://squidfunk.github.io/mkdocs-material/extensions/pymdown/#tilde) that will
strike your text through.

Ex:
```markdown
~~This is the wrong way to do~~
```

#### Symbols

If you need to add some symbols to your documentation, take care of using the 
[Smart symbols syntax](https://facelessuser.github.io/pymdown-extensions/extensions/smartsymbols/)

Ex:
`-->` will draw a nice right arrow.

#### Task lists

If you need to display a list looking like a checklist, use the 
[Task list syntax](https://squidfunk.github.io/mkdocs-material/extensions/pymdown/#tasklist)

#### Code Samples And Examples With [SuperFences]

##### Format The Code

For writing code such as samples or examples inside the documentation, we will refer to developer 
style guides depending on the language we use:

- Java : refer to our [coding convention](CODING-CONVENTIONS.md).
- JSON : use https://jsonformatter.curiousconcept.com/ to format your JSON code.
- TOML : we follow version 0.5.0 language definition.
- JavaScript : see [Google JavaScript Style Guide](https://google.github.io/styleguide/jsguide.html).

##### Include The Code In The Documentation

We use code-blocks provided by the [SuperFences] extension to present code samples and examples in the
documentation.

A basic code-block uses triple back ticks and the language name to enable syntax highlighting.

For instance a basic JSON result will be written as the following :

````markdown
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": true
}
```
````

But [SuperFences] enables to do more things. One interesting feature is the tabbed code-block.

We use it for instance to group the usage syntax and a usage example under the same block with tabs:

````markdown
```bash tab="Syntax"
$ pantheon rlp encode [--from=<FILE>] [--to=<FILE>] [--type=<type>]
```

```bash tab="File Example"
$ pantheon rlp encode --from=ibft_extra_data.json --to=extra_data_for_ibft_genesis.txt --type=IBFT_EXTRA_DATA
```

```bash tab="Standart Input/Output Example"
$ cat extra_data.json | pantheon rlp encode > rlp.txt
```
````

[SuperFences] also enables the addition of line numbers if you need to explain your code, it's easier
to be able to refer to a number without having to count the lines manually.



[MkDocs]: https://www.mkdocs.org/
[readthedocs.org]: https://readthedocs.org/
[Mkdocs Material]: https://squidfunk.github.io/mkdocs-material/
[Gitter]: https://gitter.im/PegaSysEng/pantheon
[SuperFences]: https://squidfunk.github.io/mkdocs-material/extensions/pymdown/#superfences