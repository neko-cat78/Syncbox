import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

const platform = MethodChannel('org.nslabs/irtransmitter');

void main() => runApp(const IRRemoteApp());

class IRRemoteApp extends StatelessWidget {
  const IRRemoteApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'IR Remote',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.blue,
          brightness: Brightness.dark,
        ),
      ),
      home: const RemoteScreen(),
    );
  }
}

class RemoteScreen extends StatefulWidget {
  const RemoteScreen({super.key});

  @override
  State<RemoteScreen> createState() => _RemoteScreenState();
}

class _RemoteScreenState extends State<RemoteScreen> {
  List<ColorButton> _buttons = [];
  bool _isInternal = true;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadConfig();
  }

  Future<void> _loadConfig() async {
    try {
      final jsonString = await rootBundle.loadString('assets/switch.json');
      final json = jsonDecode(jsonString);
      final colors = json['supported_colors'] as List;
      setState(() {
        _buttons = colors.map((c) => ColorButton(
          row: c['row'] as int,
          name: c['button_name'] as String,
          code: c['ir_hex_code'] as String,
          color: _parseColor(c['hex_color_value'] as String),
        )).toList();
        _loading = false;
      });
    } catch (e) {
      debugPrint('Failed to load config: $e');
      setState(() => _loading = false);
    }
  }

  Color _parseColor(String hex) {
    hex = hex.replaceFirst('#', '');
    if (hex.length == 6) hex = 'FF$hex';
    return Color(int.parse(hex, radix: 16));
  }

  Future<void> _sendCode(String hexCode) async {
    final code = int.parse(hexCode.padLeft(8, '0'), radix: 16);
    final pattern = _buildNecPattern(code);
    try {
      await platform.invokeMethod('transmit', {'list': pattern});
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('IR failed: $e')),
        );
      }
    }
  }

  List<int> _buildNecPattern(int code) {
    final pattern = <int>[9000, 4500];
    for (int i = 31; i >= 0; i--) {
      final bit = (code >> i) & 1;
      pattern.add(560);
      pattern.add(bit == 1 ? 1690 : 560);
    }
    pattern.add(560);
    return pattern;
  }

  Future<void> _setTransmitterType(bool isInternal) async {
    try {
      await platform.invokeMethod('setTransmitterType', {
        'type': isInternal ? 'INTERNAL' : 'AUDIO_1_LED',
      });
      setState(() => _isInternal = isInternal);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Switch failed: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    final rows = <int>{for (final b in _buttons) b.row}.toList()..sort();
    final grouped = <int, List<ColorButton>>{};
    for (final b in _buttons) {
      grouped.putIfAbsent(b.row, () => []).add(b);
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('IR Remote'),
        centerTitle: true,
        actions: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            child: SegmentedButton<bool>(
              segments: const [
                ButtonSegment(value: true, label: Text('Built-in')),
                ButtonSegment(value: false, label: Text('3.5mm')),
              ],
              selected: {_isInternal},
              onSelectionChanged: (v) => _setTransmitterType(v.first),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          for (final row in rows)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Row(
                children: [
                  for (final btn in (grouped[row] ?? []))
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4),
                        child: AspectRatio(
                          aspectRatio: 1,
                          child: Material(
                            color: btn.color.withValues(alpha: 0.85),
                            borderRadius: BorderRadius.circular(12),
                            child: InkWell(
                              borderRadius: BorderRadius.circular(12),
                              onTap: () => _sendCode(btn.code),
                              child: Center(
                                child: Text(
                                  btn.name,
                                  textAlign: TextAlign.center,
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 11,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}

class ColorButton {
  final int row;
  final String name;
  final String code;
  final Color color;

  const ColorButton({
    required this.row,
    required this.name,
    required this.code,
    required this.color,
  });
}
