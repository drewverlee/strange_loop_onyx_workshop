(ns workshop.jobs.test-2-0
  (:require [clojure.test :refer [deftest is]]
            [clojure.java.io :refer [resource]]
            [com.stuartsierra.component :as component]
            [workshop.launcher.dev-system :refer [onyx-dev-env]]
            [workshop.challenge-2-0 :as c]
            [workshop.workshop-utils :as u]
            [onyx.api]))

;; In this level, we're going to explore the catalog data structure.
;; Catalogs describe what the tasks of a workflow actually do, and
;; how to parameterize them in a functional and non-functional manner.
;;
;; Onyx's information model is documented in the user guide:
;; http://onyx-platform.gitbooks.io/onyx/content/doc/user-guide/information-model.html
;;
;;
;; This challenge is an already-working example to get your started.
;; It takes a stream of numbers and multiplies them by 2.
;; Go to the source file and read the comments about the catalog to get
;; an explanation about how it works.
;;
;; Try it with:
;;
;; `lein test workshop.jobs.test-2-0`
;;

(def input (map (fn [n] {:n n}) (range 10)))

(def expected-output (map (fn [n] {:n (* 2 n)}) (range 10)))

(deftest test-level-2-challenge-0
  (try
    (let [catalog (c/build-catalog)
          lifecycles (c/build-lifecycles)]
      (user/go (u/n-peers catalog c/workflow))
      (u/bind-inputs! lifecycles {:read-segments input})
      (let [peer-config (u/load-peer-config (:onyx-id user/system))
            job {:workflow c/workflow
                 :catalog catalog
                 :lifecycles lifecycles
                 :task-scheduler :onyx.task-scheduler/balanced}]
        (onyx.api/submit-job peer-config job)
        (let [[results] (u/collect-outputs! lifecycles [:write-segments])]
          (u/segments-equal? expected-output results))))
    (catch InterruptedException e
      (Thread/interrupted))
    (finally
     (user/stop))))
