package com.launcher.build.quality

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EmptyCatchBlockTest {

    private val checker = EmptyCatchBlockCheck()

    @Test
    fun should_reject_syntactically_invalid_source() {
        //given
        val source = "class Example {"

        //when & then
        assertThrows(IllegalArgumentException::class.java) {
            checker.findViolationLines(source)
        }
    }

    @Test
    fun should_allow_empty_source() {
        //given
        val source = ""

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun should_not_require_project_types_to_be_resolved() {
        //given
        val source = javaSource(
            "try {} catch (ProjectSpecificException exception) {}"
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(listOf(3), result)
    }

    @Test
    fun should_report_catch_with_multiline_annotated_parameter() {
        //given
        val source = javaSource(
            """
            try {
            } catch (
                @SuppressWarnings("unused")
                Exception exception
            ) {
            }
            """.trimIndent()
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(listOf(4), result)
    }

    @Test
    fun should_report_multiple_empty_catches_in_source_order() {
        //given
        val source = javaSource(
            """
            try {
            } catch (IllegalArgumentException first) {
            } catch (IllegalStateException second) {
            }
            """.trimIndent()
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(listOf(4, 5), result)
    }

    @Test
    fun should_ignore_catch_text_inside_text_block() {
        //given
        val delimiter = "\"\"\""

        val source = javaSource(
            listOf(
                "String sample = $delimiter",
                "catch (Exception exception) {}",
                "$delimiter;"
            ).joinToString("\n")
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(emptyList<Int>(), result)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "// catch (Exception exception) {}",
        "/* catch (Exception exception) {} */",
        "String sample = \"catch (Exception exception) {}\";",
        "char brace = '}';"
    ])
    fun should_ignore_comments_and_literals(body: String) {
        //given
        val source = javaSource(body)

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun should_allow_non_empty_catch() {
        //given
        val source = javaSource(
            """
            try {
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
            """.trimIndent()
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(emptyList<Int>(), result)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "// intentionally ignored",
        "/* intentionally ignored */"
    ])
    fun should_report_catch_containing_only_comments(comment: String) {
        //given
        val source = javaSource(
            "try {\n" +
            "} catch (Exception exception) {\n" +
            "$comment\n" +
            "}"
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(listOf(4), result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["\n", "\r\n"])
    fun should_report_multiline_empty_catch_with_correct_line_number(
        lineSeparator: String
    ) {
        //given
        val source = javaSource(
            """
                try {
                } catch (Exception exception) {
                }
            """.trimIndent()
        ).replace("\n", lineSeparator)

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(listOf(4), result)
    }

    @Test
    fun should_report_single_line_empty_catch() {
        //given
        val source = javaSource(
            "try {} catch (Exception exception) {}"
        )

        //when
        val result = checker.findViolationLines(source)

        //then
        assertEquals(listOf(3), result)
    }

    private fun javaSource(body: String): String {
        return "class Example {\n" +
                "    void execute() {\n" +
                "$body\n" +
                "    }\n" +
                "}\n"
    }
}