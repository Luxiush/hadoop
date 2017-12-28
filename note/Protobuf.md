## Protocol Buffer
* Google的编解码框架, 通过proto文件定义结构化数据, 自带代码生成器, 支持多种编程语言.

* 在Protobuf中结构化数据被称为`消息`(message)

### 基本语法

#### Message
```
/* person.proto */

package tutorial;                               // 自定义的命名空间
option java_package = "com.example.tutorial";   // 生成文件的java包名
option java_outer_classname = "PersonProtos";   // 类名

message Person {                                // 待描述的结构化数据
    required string name = 1;                   // required 表示这个字段不能为空
    required int32 id = 2;                      // optional 表示该字段可以为空
    optional string email = 3;                  // 数字“3”表示字段的数字别名

    message PhoneNumber {                       // 内部 message
        enum PhoneType {                        // 枚举
            OTHER = 0;                      // 枚举常量必须在int32范围内, 不推荐用负数
            PHONE = 1;
            MOBILE = 2;
        }

        required string number = 1;
        optional PHoneType type = 2 [default = MOBILE];  // 指定字段的默认值
    }

    repeated PhoneNumber phone = 4;             // 相当于声明一个List
}
```

##### 标识码:
* Message中的每个字段都必须指定一个唯一的标识码, 用于在编码后的二进制数据中识别出对应的字段.
* 标识码一旦使用就不能改变
* 取值范围为 [1,229-1(536,870,911)]
* [1,15]之内的标识号在编码的时候会占用`1byte`, [16,2047]之内的标识号则占用`2byte`.
* [19000,19999]为预留标识号

##### import
* 用于导入在其他.proto文件中定义的消息类型
```
import "myproject/other_protos.proto";
```

##### oneof
> 如果你的消息中有很多可选字段， 并且**同时至多一个字段会被设置**， 你可以加强这个行为，使用oneof特性节省内存.

---
#### Service
...


---
### [Java代码的生成规则](https://developers.google.com/protocol-buffers/docs/reference/java-generated)
* 基本编译命令
```
$ protoc --java_out=<dir_out> <file_in>.proto
```

##### 自动生成的函数
* 每个字段都有一个简单的`get`和`set`方法
* 每个字段都有一个`clear`方法,

---
### Java API




---
Reference:
- [Developer Guide](https://developers.google.com/protocol-buffers/docs/overview)
- [\[译\]Protobuf 语法指南](http://colobu.com/2015/01/07/Protobuf-language-guide/)
- <http://ginobefunny.com/post/learning_protobuf/>
- <https://www.ibm.com/developerworks/cn/linux/l-cn-gpb/index.html>
---


.
