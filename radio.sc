
    //////////////////////////////////////////////////////////////
    //                                                          //    
    //          I N F I N I T E D I C K  R A D I O              //
    //                                                          //         
    //                          2 0 2 1                         //
    //                                                          //
    //                                                          //
    //////////////////////////////////////////////////////////////    
    //       composed & coded by bolka for H a D i  <3          //
    //////////////////////////////////////////////////////////////

    e = (
        dir: "/matros/",
        started: IdentityDictionary.new,
        buf: IdentityDictionary.new,
        load: { arg ev, server, path, namespace = "all";
            if (ev.buf[namespace].isNil) {
                ev.buf.put(namespace, IdentityDictionary.new);
            };
            path.pathMatch.sort.do { arg file; 
                if (PathName(file).isFolder.not) {   
                    var name = file.basename.splitext.at(0).asSymbol;
                    if (ev.buf[namespace][name].isNil) {
                        ev.buf[namespace].put(name, Buffer.readChannel(server, file, channels: [0]));
                        name.postln;
                    }
                }
            }
        },
        pxrand: IdentityDictionary.new,
        consumer: { arg ev, server, cast;
            fork {
                loop {
                    cast.do { arg x; e.load(server, e.dir ++ x.asString ++ "/*_mono.wav", x) };             
                    30.wait;
                }
            }
        },
        cast: ['scenar', 'achab', 'pip', 'stubb', 'milos'],
        minute: { arg ev, v;
            60 * v
        },
        hour: {
            60 * 60 * v
        }
    );

    s.options.memSize = 2 ** 20;

    Server.default.waitForBoot({
        fork {
            1.do {
                p = ProxySpace(Server.default).push;
                x = TaskProxy.new;
                e.consumer(Server.default, e.cast);
                e.hluk = Buffer.read(Server.default, e.dir ++ "hluk.wav"); 
                Server.default.sync;
                e.cast.do { arg x;
                    e.pxrand[x] = Pxrand(e.buf[x].asArray, inf).asStream;             
                    e.pxrand_cast = Pxrand(e.cast, inf).asStream;                
                };
                p['hluk'] = NodeProxy.audio(s, 2);
                p['hluk'][0] = {
                        var sig = HPF.ar(
                            PlayBuf.ar(2, e.hluk, rate: 0.5, loop: 1) * 4, 
                            LFNoise2.ar(0.125/8, 0.5, 0.5) * 1000 + 50
                        );
                        HPF.ar((PinkNoise.ar([0.75, 0.75]) ** 6) + sig, 1400);
                };
                p['hluk'].play;
                e.cast.do { arg x;
                    var buf = e.pxrand[x].next;
                    p[x] = NodeProxy.audio(s, 2);
                    p[x][0] = { arg buf, pos = 0, treshold = 0.125, pan = 0, dur = 2, lpf = 1000;
                        var running_pos;                        
                        var sig = PlayBuf.ar(1, buf, startPos: pos, rate: BufRateScale.kr(buf), loop: 1);
                        var trig = abs(1 - DetectSilence.ar(sig, treshold, dur));
                        sig = sig * EnvGen.ar(Env.asr(0.1, 1, 1), trig);
                        sig = Limiter.ar(sig, 0.5, 1);
                        PauseSelf.kr(trig); 
                        Pan2.ar(sig * 0.5, pan);               
                    };
                    p[x].set(\buf, buf);  
                    p[x].play;
                };
                fork {
                    loop {

                        var n = 5;
                        var cast = e.pxrand_cast.nextN(n);
                        rrand(5, 30).wait;
                        cast.do {arg x;
                            var buf = e.pxrand[x].next;
                            p[x].fadeTime = 20;                                
                            p[x].vol = exprand(0.125/2, 0.5);    
                            p[x].set(\dur, rrand(0.5, 1));                 
                            p[x].set(\treshold, p[x].vol);   
                            if (0.125.coin) {                            
                                p[x].set(\pos, rrand(0, buf.numFrames));                            
                            };
                            if (0.5.coin) {      
                                p[x].set(\buf, buf);
                            };       
                            p[x].set(\pan, rrand(-0.125, 0.125));
                        
                            fork {
                                1.do {
                                    rrand(30, 60 * 3).wait;
                                    p[x].resume;                              
                                }
                            };
                        };
                        rrand(30, 60 * 2).wait;
                    };
                };
            };
        };
    });

