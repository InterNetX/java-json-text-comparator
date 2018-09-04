# JsonTextComparator

The `JsonTextComparator` was developed to get a compact textual diff between two Json sources.

## Getting Started

Currently the `JsonTextComparator` is not available on the central maven repository. But it is easy to build the library yourself. Just clone the git repository and call `mvn clean package`.


## Usage

The `JsonTextComparator` follows the fluent pattern, so it is easy to build and call the comparison.

Example:

```
String jsonExpected = "{\"val\":\"v1\"}";
String jsonActual = "{\"val\":\"v2\"}";
String diff = new JsonTextComparator(new MessageHandlerImpl("EXPECTED", "ACTUAL"))
	.setJson(jsonExpected, jsonActual).compare();
if(diff != null)
	System.out.println("Diff found: " + diff);
```

This produces the output following output: `Diff found: /val:DIFF:EXPECTED[\"v1\"];ACTUAL[\"v2\"]`

That output stands for: The node with the key *val* has two different values, "v1" in expected and "v2" in actual.

### Configuration

To customize the diff, just write your own implementation of `MessageHandler`. The available implementation `MessageHandlerImpl` is focused to have a 'one-line' diff with a small foodprint.

If there are some special nodes to compare, you can write an own implementation of `NamedNodeComparator`. Pass it to the 
`JsonTextComparator`, it delegates the comparison of named nodes to your implementation.

If you want to use `JsonTextComparator` in JUnit-tests, it can be useful to ignore some nodes. Nodes can be ignored by key (name of the node) or by path (hierarchical order of keys, e.g. /data/id).
 

## Authors

* **Thilo Schwarz ([on github](https://github.com/th-schwarz))** - *Initial work*


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details