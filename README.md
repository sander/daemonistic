# daemonistic

A Clojure library designed to work easily with [OOCSI](https://github.com/iddi/oocsi).

## Installation

Use [Leiningen](http://leiningen.org/). Add daemonistic to your `project.clj`
dependencies:

[![Clojars Project](http://clojars.org/daemonistic/latest-version.svg)](http://clojars.org/daemonistic)

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

; identify
(use 'daemonistic.services)
(def id-sender (identify! s))

; listen to identifications
(def id-receiver (identify! r)
(listen! id-receiver #(println "id:" %))

; results in:
; id: #{"receiver" "sender"}

; stop identifying sender
(stop! id-sender)

; results in:
; id: #{"receiver"}

; stop listening
(stop! id-receiver)

; sync (wait a bit between both commands)
(listen! (simple-sync! s) #(println "s" %))
(listen! (simple-sync! r) #(println "r" %))
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
