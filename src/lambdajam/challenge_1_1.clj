(ns lambdajam.challenge-1-1
  (:require [lambdajam.workshop-utils :as u]))

(def batch-size 20)

;;; Workflows ;;;

;;; <<< FILL ME IN >>>

(def workflow
  [[:read-segments :cube-n]
   [:cube-n :add-ten]
   [:add-ten :multiply-by-5]
   [:multiply-by-5 :write-segments]])

;;; <<< FILL ME IN >>>

;;; Catalogs ;;;

(def catalog
  [{:onyx/name :read-segments
    :onyx/ident :core.async/read-from-chan
    :onyx/type :input
    :onyx/medium :core.async
    :onyx/batch-size batch-size
    :onyx/max-peers 1
    :onyx/doc "Reads segments from a core.async channel"}

   {:onyx/name :cube-n
    :onyx/fn :lambdajam.challenge-1-1/cube-n
    :onyx/type :function
    :onyx/batch-size batch-size
    :onyx/doc "Mutiply :n by itself twice"}

   {:onyx/name :add-ten
    :onyx/fn :lambdajam.challenge-1-1/add-ten
    :onyx/type :function
    :onyx/batch-size batch-size
    :onyx/doc "Add 10 to :n"}

   {:onyx/name :multiply-by-5
    :onyx/fn :lambdajam.challenge-1-1/multiply-by-5
    :onyx/type :function
    :onyx/batch-size batch-size
    :onyx/doc "Multiply :n by 5"}

   {:onyx/name :write-segments
    :onyx/ident :core.async/write-to-chan
    :onyx/type :output
    :onyx/medium :core.async
    :onyx/batch-size batch-size
    :onyx/max-peers 1
    :onyx/doc "Writes segments to a core.async channel"}])

;;; Functions ;;;

(defn cube-n [segment]
  (update-in segment [:n] (partial * (:n segment) (:n segment))))

(defn add-ten [segment]
  (update-in segment [:n] (partial + 10)))

(defn multiply-by-5 [segment]
  (update-in segment [:n] (partial * 5)))

;;; Lifecycles ;;;

(defn inject-reader-ch [event lifecycle]
  {:core.async/chan (u/get-input-channel (:core.async/id lifecycle))})

(defn inject-writer-ch [event lifecycle]
  {:core.async/chan (u/get-output-channel (:core.async/id lifecycle))})

(def reader-lifecycle
  {:lifecycle/before-task-start inject-reader-ch})

(def writer-lifecycle
  {:lifecycle/before-task-start inject-writer-ch})

(defn build-lifecycles []
  [{:lifecycle/task :read-segments
    :lifecycle/calls :lambdajam.challenge-1-1/reader-lifecycle
    :onyx/doc "Injects the core.async reader channel"}

   {:lifecycle/task :read-segments
    :lifecycle/calls :onyx.plugin.core-async/reader-calls
    :core.async/id (java.util.UUID/randomUUID)
    :onyx/doc "core.async plugin base lifecycle"}

   {:lifecycle/task :write-segments
    :lifecycle/calls :lambdajam.challenge-1-1/writer-lifecycle
    :onyx/doc "Injects the core.async writer channel"}

   {:lifecycle/task :write-segments
    :lifecycle/calls :onyx.plugin.core-async/writer-calls
    :core.async/id (java.util.UUID/randomUUID)
    :onyx/doc "core.async plugin base lifecycle"}])