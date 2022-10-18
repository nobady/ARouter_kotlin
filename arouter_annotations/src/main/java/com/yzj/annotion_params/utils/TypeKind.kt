package com.yzj.annotion_params.utils

/**
 * <pre>
 *     @author : lvtf
 *     e-mail : tengfei_lv@yunzhijia.com
 *     time   : 2022/09/20
 *     desc   :
 *     version: 1.0
 * </pre>
 */
enum class TypeKind {

    // Base type
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    CHAR,
    FLOAT,
    DOUBLE,

    // Other type
    STRING,
    SERIALIZABLE,
    PARCELABLE,
    OBJECT;
}