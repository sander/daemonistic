# daemonistic

A Clojure library designed to work easily with [OOCSI](https://github.com/iddi/oocsi).

## Installation

Use [Leiningen](http://leiningen.org/). Install OOCSI first:

```sh
git clone https://github.com/sander/oocsi.git
cd oocsi/client
lein install
```

Then install daemonistic by running `lein install` in this directory.
You can now add it to your `project.clj` dependencies:

```clj
:dependencies [[daemonistic "0.1.0-SNAPSHOT"]
```

## Usage

Try it at the REPL:

```clj
(use 'daemonistic.oocsi)

(def s (connected-client! "sender"))
(def r (connected-client! "receiver"))

(def mychan (subscribe! r "mychan"))

; send! accepts strings and maps with string keys
(send! s "mychan" "Hello, World!")
(send! s "mychan" {"foo" "bar"})

; use standard core.async methods on the channel
(require '[clojure.core.async :refer [<!! <! go-loop]])
(<!! mychan)
(<!! mychan)

; returns:
; {:sender "sender", :data {"data" "Hello, World!"}, :timestamp "1410809172930"}
; {:sender "sender", :data {"foo" "bar"}, :timestamp "1410809177515"}

; watch the channel asynchronously
(go-loop []
  (println "received: " (<! mychan))
  (recur))
```

To run the chat example, run a few clients using

```sh
lein run -m examples.chat <username>
```

where `<username>` is a unique name.

## License

Copyright © 2014 Sander Dijkhuis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
