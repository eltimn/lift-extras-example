# lift-extras-example
Example usage of lift-extras

## Run

Requires [SBT](http://www.scala-sbt.org/)

You will need to open 2 terminals in the project root directory.

In the first terminal run ```sbt``` and then ```~start``` in the sbt repl.

In the second terminal run ```./gulp watch```.

Open you browser at http://localhost:3000/

### KoAlerts

A snippet that uses a Knockout.js `View Model` to display notices. Does not rely on Bootstrap. Allows for any html that complies with the `View Model`. See [KoAlerts.scala](https://github.com/eltimn/lift-extras-example/blob/master/src/main/scala/code/snippet/KoAlerts.scala) and [alerts.html](https://github.com/eltimn/lift-extras-example/blob/master/src/main/webapp/templates-hidden/alerts.html) for details.

[Download JavaScript](https://raw.github.com/eltimn/lift-extras-example/master/src/main/assets/js/KoAlerts.js)
