wisdom ~ Web Is Dynamic and Modular
=======

Building Wisdom
===============

Because Wisdom is built on top of Wisdom, building has to be done in two steps. First,
the main API and the Wisdom Maven Plugin are built. Then, service implementations and technical feature are processed
 and aggregated inside a `base runtime`. Finally, a third step is building advanced technical services and support
 and compose the `Wisdom Runtime`.

    mvn clean install -Pbase
    mvn clean install

_Why having two runtime projects?_ Because Wisdom services are tested against Wisdom itself. So to avoid loops,
we test them on the base runtime.
