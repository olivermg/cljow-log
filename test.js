function test() {
    console.log("JS START");

    ow.logging.api.alpha.js.with_checkpoint("check1", () => {
        ow.logging.api.alpha.js.with_data({ data1: 111 }, () => {
            ow.logging.api.alpha.js.warn("WARN ME!", { warn1: 222});
        });
    });

    console.log("JS END");
}

module.exports = {
    test: test
};
