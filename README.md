# Clojurescript-SPA-Proiect-Afaceri-El.
This project was build using Clojure with the following dependencies : clojurescript, cljsjs/bootstrap, cljsjs/jquery, reagent, reagent-forms.

The project was compiled using Leiningen.

You can find the HTML file in recources / index.html

An API wrote in ClojureScript that generates 25 different widgets ( text-fields, tables, tree-tables etc.) that can be used in a Reactive Website. My program converts my functions directly into Html, CSS, JavaScript and mounts them in a website. Those components are decorated with Bootstrap, and they are reactive ( I used Reagent library that is a ClojureScript interface to React ). Using this API, I made a demo : I have 2 persons hardcoded in my program. Each person have one or more accounts. We can add/edit/remove any person. When editing a person, its accounts are listed in a pop-up that contains a table. From this table, we can add/edit/remove elements via another dialog.

All the changes are automatically updated.
